package com.project.yogerOrder.product;

import static org.awaitility.Awaitility.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.event.ProductEventType;
import com.project.yogerOrder.product.event.ProductUpdatedEvent;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.service.ProductService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ProductIntegrationTest extends UsingTestContainerTest {

	@Autowired
	ProductService productService;

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;

	@Test
	void productUpdateEvent() {
		// given
		Long productId = 1L;
		Integer stock = 50;
		Integer price = 3000;

		// when
		kafkaTemplate.executeInTransaction(kafkaTemplate ->
			kafkaTemplate.send(
				ProductTopic.UPDATED,
				new ProductUpdatedEvent(
					productId,
					UUID.randomUUID().toString(),
					ProductEventType.UPDATED,
					new ProductUpdatedEvent.ProductUpdatedData("proName", stock, price),
					LocalDateTime.now()
				))
		);

		// then
		await()
			.pollInterval(Duration.ofSeconds(1))
			.atMost(Duration.ofSeconds(30))
			.ignoreException(ProductNotFoundException.class)
			.untilAsserted(() -> {
				ProductResponseDTO productResponseDTO = productService.findById(productId);

				Assertions.assertEquals(productId, productResponseDTO.id());
				Assertions.assertEquals(stock, productResponseDTO.stock());
				Assertions.assertEquals(price, productResponseDTO.price());
			});
	}

}
