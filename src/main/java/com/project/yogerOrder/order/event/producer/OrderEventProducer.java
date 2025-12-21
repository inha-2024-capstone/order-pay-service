package com.project.yogerOrder.order.event.producer;

import java.util.List;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.entity.OrderState;

public interface OrderEventProducer {

    void publishEventByState(OrderEntity orderEntity, OrderState beforeState);

    void publishOrderCreatedEvent(OrderEntity orderEntity);

    void publishOrderDeductionAfterCanceledEvent(OrderEntity orderEntity);

    void publishPaymentCompletedAfterOrderCanceledEvent(OrderEntity orderEntity);
    
    void publishConfirmProductReservationEvent(String orderId, Long buyerId, List<OrderItem> orderItems);
}
