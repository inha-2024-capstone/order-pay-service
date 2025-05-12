package com.project.yogerOrder.order.dto.response;

import java.util.List;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderResponseDTO(@NotNull String orderId, @NotEmpty List<OrderItem> orderItems) {

    public static OrderResponseDTO from(OrderEntity orderEntity) {
        return new OrderResponseDTO(
            orderEntity.getId(),
            orderEntity.getOrderItems()
        );
    }
}
