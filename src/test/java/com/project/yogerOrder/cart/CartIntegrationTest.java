package com.project.yogerOrder.cart;

import static org.awaitility.Awaitility.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import com.project.yogerOrder.cart.controller.CartController;
import com.project.yogerOrder.cart.dto.request.DeleteProductFromCartRequestDTO;
import com.project.yogerOrder.cart.dto.request.UpsertProductToCartRequestDTO;
import com.project.yogerOrder.cart.dto.response.CartResponseDTO;
import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.event.config.OrderTopic;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.event.OrderCompletedEvent;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CartIntegrationTest extends UsingTestContainerTest {

	@Autowired
	CartController cartController;

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;


	Long userId = 1L;
	UpsertProductToCartRequestDTO requestDTO1 = new UpsertProductToCartRequestDTO(1L, 111);
	UpsertProductToCartRequestDTO updateRequestDTO = new UpsertProductToCartRequestDTO(1L, 1111);
	UpsertProductToCartRequestDTO requestDTO2 = new UpsertProductToCartRequestDTO(22L, 222);
	UpsertProductToCartRequestDTO requestDTO3 = new UpsertProductToCartRequestDTO(33L, 333);

	@Test
	void upsertItem() {
		// given
		cartController.upsertItem(userId, requestDTO1);
		cartController.upsertItem(userId, updateRequestDTO);
		cartController.upsertItem(userId, requestDTO2);

		// when
		List<CartResponseDTO.CartResponseData> cartItems = cartController.getCart(userId).getBody().cartItems();

		// then
		Assertions.assertThat(cartItems).contains(new CartResponseDTO.CartResponseData(updateRequestDTO.productId(), updateRequestDTO.quantity()));
		Assertions.assertThat(cartItems).contains(new CartResponseDTO.CartResponseData(requestDTO2.productId(), requestDTO2.quantity()));
	}

	@Test
	void getCart() {
		// when
		List<CartResponseDTO.CartResponseData> cartItems = cartController.getCart(userId).getBody().cartItems();

		// then
		Assertions.assertThat(cartItems).isEmpty();
	}


	@Test
	void deleteItemsByApi() {
		// given
		Long notPresentProductId = 1234L;

		cartController.upsertItem(userId, requestDTO1);
		cartController.upsertItem(userId, requestDTO2);
		cartController.upsertItem(userId, requestDTO3);

		cartController.deleteItems(userId, new DeleteProductFromCartRequestDTO(List.of(requestDTO1.productId(), requestDTO2.productId(), notPresentProductId)));

		// when
		List<CartResponseDTO.CartResponseData> cartItems = cartController.getCart(userId).getBody().cartItems();

		// then
		Assertions.assertThat(cartItems).noneMatch(item -> item.productId().equals(requestDTO1.productId()));
		Assertions.assertThat(cartItems).noneMatch(item -> item.productId().equals(requestDTO2.productId()));
		Assertions.assertThat(cartItems).contains(new CartResponseDTO.CartResponseData(requestDTO3.productId(), requestDTO3.quantity()));
	}

	@Test
	void deleteItemsByEvent() {
		// given
		Integer totalPrice = 1000;

		cartController.upsertItem(userId, requestDTO1);
		cartController.upsertItem(userId, requestDTO2);
		cartController.upsertItem(userId, requestDTO3);

		OrderItem data1 = new OrderItem(requestDTO1.productId(), requestDTO1.quantity());
		OrderItem data2 = new OrderItem(requestDTO2.productId(), requestDTO2.quantity());


		OrderEntity orderEntity = OrderEntity.createPendingOrder(List.of(data1, data2), totalPrice, userId);
		OrderCompletedEvent orderCompletedEvent = OrderCompletedEvent.from(orderEntity);

		// when
		kafkaTemplate.executeInTransaction(kafkaTemplate ->
			kafkaTemplate.send(OrderTopic.getTopicByEvent(orderCompletedEvent.eventType()), orderCompletedEvent)
		);

		// then
		await()
			.pollInterval(Duration.ofSeconds(1))
			.atMost(Duration.ofSeconds(30))
			.untilAsserted(() -> {
				List<CartResponseDTO.CartResponseData> cartItems = cartController.getCart(userId).getBody().cartItems();

				Assertions.assertThat(cartItems).noneMatch(item -> item.productId().equals(requestDTO1.productId()));
				Assertions.assertThat(cartItems).noneMatch(item -> item.productId().equals(requestDTO2.productId()));
				Assertions.assertThat(cartItems).contains(new CartResponseDTO.CartResponseData(requestDTO3.productId(), requestDTO3.quantity()));
			});
	}

}

