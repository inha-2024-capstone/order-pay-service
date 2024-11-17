package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.exception.OrderRepositoryException;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;


    // CREATE
    // 주문을 생성하는 과정을 먼저 진행하면, 주문이 생성되고, 재고 감소가 실패하기 전에 주문이 결제되면 문제가 생기기 때문에 재고 감소 먼저 진행
    // + 일반적으로 외부 서비스에 문제가 생기는 경우가 많기 때문에 외부 서비스 호출 먼저
    //TODO error handling, circuit breaker
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

}
