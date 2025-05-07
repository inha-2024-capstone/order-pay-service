package com.project.yogerOrder.cart.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpsertProductToCartRequestDTO(@NotNull Long productId, @NotNull Integer quantity) {
}
