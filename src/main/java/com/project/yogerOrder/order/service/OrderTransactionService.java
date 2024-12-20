package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderTransactionService {

    private final OrderRepository orderRepository;

    @Transactional
    public void updateToExpiredState(OrderEntity orderEntity) {
        orderEntity.reject();
        orderRepository.save(orderEntity);
    }

    @Transactional
    public void updateToErrorState(OrderEntity orderEntity) {
        orderEntity.error();
        orderRepository.save(orderEntity);
    }
}
