package com.project.yogerOrder.cart.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record DeleteProductFromCartRequestDTO(@NotEmpty List<Long> productIds) {
}
