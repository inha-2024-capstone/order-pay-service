package com.project.yogerOrder.cart.dto.response;

import java.util.List;

import com.project.yogerOrder.cart.entity.CartEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CartResponseDTO(@NotEmpty List<CartResponseData> cartItems) {

	public record CartResponseData(@NotNull Long productId, @NotNull Integer quantity) {}

	public static CartResponseDTO from(CartEntity cart) {
		return new CartResponseDTO(cart.getItems()
			.stream().map(entry -> new CartResponseData(entry.getKey(), entry.getValue()))
			.toList()
		);
	}
}
