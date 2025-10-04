package com.project.yogerOrder.order.event.producer;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.product.event.ConfirmProductReservationEvent;
import com.project.yogerOrder.product.event.config.ProductTopic;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderKafkaEventProducer {
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	public void publishConfirmProductReservationEvent(String orderId, Long buyerId, List<OrderItem> orderItems) {
		ConfirmProductReservationEvent event = ConfirmProductReservationEvent.of(orderId, buyerId, orderItems);
		
		kafkaTemplate.send(ProductTopic.getTopicByEventType(event.eventType()), event);
	}
	
}

