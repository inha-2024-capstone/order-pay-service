package com.project.yogerOrder.order.service;

import com.project.yogerOrder.global.util.db.MongoTransactional;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.event.producer.OrderEventProducer;
import com.project.yogerOrder.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTransactionService {

    private final OrderRepository orderRepository;

    private final OrderEventProducer orderEventProducer;

    @MongoTransactional
    public String saveOrder(OrderEntity pendingOrder) {
        OrderEntity orderEntity = orderRepository.save(pendingOrder);

        orderEventProducer.publishOrderCreatedEvent(orderEntity);

        return orderEntity.getId();
    }


}
