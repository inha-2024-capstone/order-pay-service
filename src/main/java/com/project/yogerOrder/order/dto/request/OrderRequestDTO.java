package com.project.yogerOrder.order.dto.request;


import jakarta.validation.constraints.NotNull;

public record OrderRequestDTO(@NotNull Integer quantity) {
}
