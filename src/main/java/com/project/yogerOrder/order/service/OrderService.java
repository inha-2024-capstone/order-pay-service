package com.project.yogerOrder.order.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.project.yogerOrder.global.util.lock.OptimisticLockRetry;
import com.project.yogerOrder.order.config.OrderConfig;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.request.OrdersCountRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.event.producer.OrderEventProducer;
import com.project.yogerOrder.order.exception.OrderNotFoundException;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.order.util.stateMachine.OrderStateChangeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderConfig config;

    private final OrderEventProducer orderEventProducer;


    // CREATE
    @Transactional
    public Long orderProduct(Long userId, Long productId, OrderRequestDTO orderRequestDTO) {
        OrderEntity pendingOrder = OrderEntity.createPendingOrder(productId, orderRequestDTO.quantity(), userId);
        OrderEntity orderEntity = orderRepository.save(pendingOrder);

        orderEventProducer.publishOrderCreatedEvent(orderEntity);

        return orderEntity.getId();
    }


    // READ
    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public Boolean isPayable(OrderEntity orderEntity) {
        return orderEntity.isPayable(config.validTime());
    }

    @Transactional(readOnly = true)
    public OrderCountResponseDTOs countOrdersByProductIds(OrdersCountRequestDTO ordersCountRequestDTO) {
        List<OrderCountResponseDTO> orderCountResponseDTOS = ordersCountRequestDTO.productIds().stream().map(productId ->
                new OrderCountResponseDTO(
                        productId,
                        orderRepository.countAllByProductIdAndState(productId, OrderState.COMPLETED)
                )
        ).toList();

        return new OrderCountResponseDTOs(orderCountResponseDTOS);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTOs findApprovedOrdersByUserId(Long userId) {
        return OrderResponseDTOs.from(orderRepository.findAllByBuyerIdAndState(userId, OrderState.COMPLETED));
    }

    // UPDATE
    @OptimisticLockRetry
    public void updateByDeductionSuccess(Long orderId) {
        OrderEntity orderEntity = findById(orderId);

        if (orderEntity.getState() == OrderState.CANCELED) {
            orderEventProducer.publishOrderDeductionAfterCanceledEvent(orderEntity);
            return;
        }

        updateByStateChange(orderEntity, OrderStateChangeEvent.STOCK_DEDUCTED);
    }

    @OptimisticLockRetry
    public void updateByDeductionFail(Long orderId) {
        updateByStateChange(findById(orderId), OrderStateChangeEvent.STOCK_DEDUCT_FAILED);
    }

    @OptimisticLockRetry
    public void updateByPaymentCompleted(Long orderId) {
        OrderEntity orderEntity = findById(orderId);

        if (orderEntity.getState() == OrderState.CANCELED) {
            orderEventProducer.publishPaymentCompletedAfterOrderCanceledEvent(orderEntity);
            return;
        }

        updateByStateChange(orderEntity, OrderStateChangeEvent.PAID);
    }

    @OptimisticLockRetry
    public void updateByPaymentCanceled(Long orderId) {
        updateByStateChange(findById(orderId), OrderStateChangeEvent.PAYMENT_CANCELED);
    }

    // 주기적 pending 상태 order를 만료 상태로 변경하고 상품 재고 release
    @Transactional
    @Scheduled(cron = "${order.cron.expiration}")
    @SchedulerLock(name = "orderExpirationSchedule", lockAtMostFor = "PT50S", lockAtLeastFor = "PT40S")
    public void orderExpirationSchedule() {
        OrderState.getPayableStates().forEach(orderState ->
                orderRepository.findAllByState(orderState)
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
