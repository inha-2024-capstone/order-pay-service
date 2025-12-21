package com.project.yogerOrder.order.event.producer;

import java.util.List;

import org.springframework.stereotype.Component;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.entity.OrderState;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventProducerImpl implements OrderEventProducer {
	
	private final OrderOutboxEventProducer orderOutboxEventProducer;
	
	private final OrderKafkaEventProducer orderKafkaEventProducer;
	
	@Override
	public void publishEventByState(OrderEntity orderEntity, OrderState beforeState) {
		orderOutboxEventProducer.publishEventByState(orderEntity, beforeState);
	}
	
	@Override
	public void publishOrderCreatedEvent(OrderEntity orderEntity) {
		orderOutboxEventProducer.publishOrderCreatedEvent(orderEntity);
	}
	
	@Override
	public void publishOrderDeductionAfterCanceledEvent(OrderEntity orderEntity) {
		orderOutboxEventProducer.publishOrderDeductionAfterCanceledEvent(orderEntity);
	}
	
	@Override
	public void publishPaymentCompletedAfterOrderCanceledEvent(OrderEntity orderEntity) {
		orderOutboxEventProducer.publishPaymentCompletedAfterOrderCanceledEvent(orderEntity);
	}
	
	@Override
	public void publishConfirmProductReservationEvent(String orderId, Long buyerId, List<OrderItem> orderItems) {
		orderKafkaEventProducer.publishConfirmProductReservationEvent(orderId, buyerId, orderItems);
	}
}
