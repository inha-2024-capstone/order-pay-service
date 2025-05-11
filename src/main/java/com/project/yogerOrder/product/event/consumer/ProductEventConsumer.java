package com.project.yogerOrder.product.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.project.yogerOrder.global.config.KafkaConfig;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.dto.request.UpsertProductRequestDTO;
import com.project.yogerOrder.product.event.ProductUpdatedEvent;
import com.project.yogerOrder.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

	private final ProductService productService;

	@KafkaListener(topics = ProductTopic.UPDATED, groupId = KafkaConfig.PRODUCT_GROUP,
		containerFactory = KafkaConfig.KafkaConsumerConfig.PRODUCT_UPDATED_FACTORY)
	public void productUpdated(ProductUpdatedEvent event, Acknowledgment acknowledgment) {
		productService.upsertProduct(new UpsertProductRequestDTO(
			event.productId(),
			event.getName(),
			event.getStock(),
			event.getPrice()
		));

		acknowledgment.acknowledge();
	}
}
