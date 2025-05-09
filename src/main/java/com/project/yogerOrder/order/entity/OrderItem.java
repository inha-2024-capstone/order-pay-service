package com.project.yogerOrder.order.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItem(@NotNull Long productId, @Min(1) @NotNull Integer quantity) {
}