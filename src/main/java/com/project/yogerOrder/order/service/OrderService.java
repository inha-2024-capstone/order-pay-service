package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.config.OrderConfig;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.exception.OrderNotFoundException;
import com.project.yogerOrder.order.exception.OrderRepositoryException;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.ProductServerStateException;
import com.project.yogerOrder.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderConfig config;

    private final ProductService productService;

    private final OrderTransactionService orderTransactionService;


    // CREATE
    // 주문을 생성하는 과정을 먼저 진행하면, 주문이 생성되고, 재고 감소가 실패하기 전에 주문이 결제되면 문제가 생기기 때문에 재고 감소 먼저 진행
    // + 일반적으로 외부 서비스에 문제가 생기는 경우가 많기 때문에 외부 서비스 호출 먼저
    @Transactional
    public Long orderProduct(Long userId, Long productId, OrderRequestDTO orderRequestDTO) {
        productService.decreaseStock(productId, orderRequestDTO.quantity());

        try {
            OrderEntity pendingOrder = OrderEntity.createPendingOrder(productId, orderRequestDTO.quantity(), userId);
            return orderRepository.save(pendingOrder).getId();
        } catch (Exception e) {
            // 보상 트랜잭션
            productService.increaseStock(productId, orderRequestDTO.quantity());
            throw new OrderRepositoryException();
        }
    }


    // READ
    @Transactional(readOnly = true)
    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Boolean isPayable(OrderEntity orderEntity) {
        return orderEntity.isPayable(config);
    }

    @Transactional(readOnly = true)
    public Integer countOrdersByProductId(Long productId) {
        return orderRepository.countAllByProductIdAndState(productId, OrderState.APPROVED);
    }

    // DELETE


    // UPDATE
    @Transactional
    public void updateStatusToPaidById(Long id) {
        findById(id).setState(OrderState.APPROVED);
    }

    // 주기적 pending 상태 order를 만료 상태로 변경하고 상품 재고 release
    @Scheduled(cron = "${order.cron.expiration}")
    public void orderExpirationSchedule() {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(config.timeLimit());

        orderRepository.findAllByState(OrderState.PENDING)
                .parallelStream()
                .filter(orderEntity -> orderEntity.getCreatedTime().isBefore(timeLimit))
                .forEach(orderEntity -> {
                    try {
                        // 외부 서비스 호출에 성공하면 DB를 업데이트하도록 변경
                        productService.increaseStock(orderEntity.getProductId(), orderEntity.getQuantity()); //TODO checksum 구현
                    } catch (ProductNotFoundException e) { //TODO 추가 처리 필요
                        orderTransactionService.updateToErrorState(orderEntity); //TODO 나중 별도 처리를 위한 error 상태로 전환

                        log.error("Order(id={})'s product (id={}) is not present in productService, and exception message: {}",
                                orderEntity.getId(), orderEntity.getProductId(), e.getMessage());

                        return;
                    } catch (ProductServerStateException e) {
                        log.error("Error occurred from product server");
                        return;
                    } catch (Exception e) {
                        log.error("Unknown error occurred from product server");
                        return;
                    }

                    try {
                        orderTransactionService.updateToExpiredState(orderEntity);
                    } catch (Exception e) {
                        productService.decreaseStock(orderEntity.getProductId(), orderEntity.getQuantity());
                        throw new OrderRepositoryException();
                    }
                });

        log.info("Pending order expiration schedule executed");
    }
}
