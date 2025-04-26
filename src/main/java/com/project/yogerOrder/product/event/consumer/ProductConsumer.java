package com.project.yogerOrder.product.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.project.yogerOrder.global.config.KafkaConfig;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductUpdatedEvent;
import com.project.yogerOrder.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductConsumer {

	private final ProductService productService;

	@KafkaListener(topics = ProductTopic.UPDATED, groupId = KafkaConfig.PRODUCT_GROUP,
		containerFactory = KafkaConfig.KafkaConsumerConfig.PRODUCT_UPDATED_FACTORY)
	public void productDeductionFailed(ProductUpdatedEvent event, Acknowledgment acknowledgment) {
		productService.updateStock(event.productId(), event.data().currentStock());

		acknowledgment.acknowledge();
	}
}
