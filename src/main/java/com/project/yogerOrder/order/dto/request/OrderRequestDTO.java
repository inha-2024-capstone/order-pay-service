package com.project.yogerOrder.order.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderRequestDTO(@NotNull String orderRequestId, @NotEmpty List<OrderItemRequestDTO> orderItems) {
}
