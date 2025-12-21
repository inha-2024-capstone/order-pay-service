package com.project.yogerOrder.product.dto.request;

import java.util.List;

import com.project.yogerOrder.order.entity.OrderItem;

import jakarta.validation.constraints.NotNull;

public record ReserveProductsRequestDTO(@NotNull String orderId, @NotNull List<OrderItem> orderItems) {
}
