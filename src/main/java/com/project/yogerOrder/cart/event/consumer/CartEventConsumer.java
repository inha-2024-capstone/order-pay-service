package com.project.yogerOrder.cart.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.project.yogerOrder.cart.dto.request.DeleteProductFromCartRequestDTO;
import com.project.yogerOrder.cart.service.CartService;
import com.project.yogerOrder.global.config.KafkaConfig;
import com.project.yogerOrder.order.event.config.OrderTopic;
import com.project.yogerOrder.order.dto.request.OrderItemRequestDTO;
import com.project.yogerOrder.order.event.OrderCompletedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartEventConsumer {

	private final CartService cartService;


	@KafkaListener(topics = OrderTopic.COMPLETED, groupId = KafkaConfig.CART_GROUP,
		containerFactory = KafkaConfig.KafkaConsumerConfig.ORDER_COMPLETED_FACTORY)
	public void orderCompleted(OrderCompletedEvent event, Acknowledgment acknowledgment) {
		DeleteProductFromCartRequestDTO requestDTO = new DeleteProductFromCartRequestDTO(event.getOrderItems()
			.stream().map(OrderItemRequestDTO::productId)
			.toList()
		);
		cartService.deleteItems(event.getUserId(), requestDTO);

		acknowledgment.acknowledge();
	}
}
