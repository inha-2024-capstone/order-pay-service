package com.project.yogerOrder.order.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.project.yogerOrder.global.util.db.MongoTransactional;
import com.project.yogerOrder.global.util.lock.OptimisticLockRetry;
import com.project.yogerOrder.order.config.OrderConfig;
import com.project.yogerOrder.order.dto.request.ConfirmReservationsRequestDTO;
import com.project.yogerOrder.order.dto.request.OrderItemRequestDTO;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.event.producer.OrderEventProducer;
import com.project.yogerOrder.order.exception.OrderNotFoundException;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.order.util.duplicate.exception.OrderDuplicatedException;
import com.project.yogerOrder.order.util.duplicate.service.OrderDuplicateCheckService;
import com.project.yogerOrder.order.util.stateMachine.OrderStateChangeEvent;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderConfig config;

    private final OrderEventProducer orderEventProducer;

    private final OrderDuplicateCheckService orderDuplicateCheckService;

    private final ProductService productService;


    // CREATE
    @MongoTransactional
    public String orderProduct(Long userId, OrderRequestDTO orderRequestDTO) {
        try {
            orderDuplicateCheckService.register(orderRequestDTO.orderRequestId());
        } catch (OrderDuplicatedException e) {
            log.warn("Duplicated order request. orderRequestId: {}", orderRequestDTO.orderRequestId());
            throw e;
        }

        List<OrderItem> orderItems = orderRequestDTO.orderItems()
            .stream().map(OrderItemRequestDTO::toOrderItem)
            .toList();

        OrderEntity pendingOrder = OrderEntity.createPendingOrder(orderItems, calculateTotalPrice(orderItems), userId);
        
        productService.reserveStocks(pendingOrder.getId(), pendingOrder.getOrderItems());
        
        OrderEntity orderEntity = orderRepository.save(pendingOrder);

        orderEventProducer.publishOrderCreatedEvent(orderEntity);

        return orderEntity.getId();
    }

    private Integer calculateTotalPrice(List<OrderItem> orderItems) {
        Map<Long, Integer> idToPrice = productService.findByIds(orderItems.stream().map(OrderItem::productId).toList())
            .stream().collect(Collectors.toMap(ProductResponseDTO::id, ProductResponseDTO::price));

        return orderItems.stream().mapToInt(item -> {
            Integer price = idToPrice.get(item.productId());
            if (price == null) {
                throw new ProductNotFoundException();
            }

            return price * item.quantity();
        }).sum();
    }
    
    public void confirmReservations(ConfirmReservationsRequestDTO requestDTO) {
        orderEventProducer.publishConfirmProductReservationEvent(
            requestDTO.orderId(),
            requestDTO.buyerId(),
            requestDTO.orderItemRequestDTOs().stream().map(OrderItemRequestDTO::toOrderItem).collect(Collectors.toList())
        );
    }


    // READ
    public OrderEntity findById(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public Boolean isPayable(OrderEntity orderEntity) {
        return orderEntity.isPayable(config.validTime());
    }

    @MongoTransactional(readOnly = true)
    public OrderResponseDTOs findApprovedOrdersByUserId(Long userId) {
        return OrderResponseDTOs.from(orderRepository.findAllByBuyerIdAndState(userId, OrderState.COMPLETED));
    }

    // UPDATE
    @OptimisticLockRetry
    @MongoTransactional
    public void updateByDeductionSuccess(String orderId) {
        OrderEntity orderEntity = findById(orderId);

        if (orderEntity.getState() == OrderState.CANCELED) {
            orderEventProducer.publishOrderDeductionAfterCanceledEvent(orderEntity);
            return;
        }

        updateByStateChange(orderEntity, OrderStateChangeEvent.STOCK_DEDUCTED);
    }

    @OptimisticLockRetry
    @MongoTransactional
    public void updateByDeductionFail(String orderId) {
        updateByStateChange(findById(orderId), OrderStateChangeEvent.STOCK_DEDUCT_FAILED);
    }

    @OptimisticLockRetry
    @MongoTransactional
    public void updateByPaymentCompleted(String orderId) {
        OrderEntity orderEntity = findById(orderId);

        if (orderEntity.getState() == OrderState.CANCELED) {
            orderEventProducer.publishPaymentCompletedAfterOrderCanceledEvent(orderEntity);
            return;
        }

        updateByStateChange(orderEntity, OrderStateChangeEvent.PAID);
    }

    @OptimisticLockRetry
    @MongoTransactional
    public void updateByPaymentCanceled(String orderId) {
        updateByStateChange(findById(orderId), OrderStateChangeEvent.PAYMENT_CANCELED);
    }

    // 주기적 pending 상태 order를 만료 상태로 변경하고 상품 재고 release
    @Scheduled(cron = "${order.cron.expiration}")
    @MongoTransactional
    @SchedulerLock(name = "orderExpirationSchedule", lockAtMostFor = "PT50S", lockAtLeastFor = "PT40S")
    public void orderExpirationSchedule() {
        OrderState.getPayableStates().forEach(orderState -> orderRepository.findAllByState(orderState)
            .parallelStream()
            .filter(orderEntity -> !orderEntity.isPayable(config.validTime()))
            .forEach(this::updateByExpiration)
        );

        log.info("Pending order expiration schedule successfully executed");
    }


    private void updateByExpiration(OrderEntity orderEntity) {
        updateByStateChange(orderEntity, OrderStateChangeEvent.EXPIRED);
    }

    private void updateByStateChange(OrderEntity orderEntity, OrderStateChangeEvent orderStateChangeEvent) {
        OrderState beforeState = orderEntity.getState();
        if (!orderEntity.changeStateIfChangeable(orderStateChangeEvent)) {
            log.debug("Order is already in the target state. orderId: {}", orderEntity.getId());
            return;
        }

        orderRepository.save(orderEntity);

        orderEventProducer.publishEventByState(orderEntity, beforeState);
    }
}
