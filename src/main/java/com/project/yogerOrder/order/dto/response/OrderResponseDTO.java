package com.project.yogerOrder.order.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderResponseDTO(@NotBlank String orderId) {

    public OrderResponseDTO(@NotNull Long orderId) {
        this(String.valueOf(orderId));
    }
}
