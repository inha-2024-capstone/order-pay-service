package com.project.yogerOrder.order.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderCreatedEvent(@NotNull String orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                @NotNull OrderCreatedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static OrderCreatedEvent from(OrderEntity orderEntity) {
        return new OrderCreatedEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.CREATED,
                OrderCreatedData.of(orderEntity.getBuyerId(), orderEntity.getOrderItems()),
                LocalDateTime.now()
        );
    }

    private record OrderCreatedData(@NotNull Long userId, @NotEmpty List<OrderItemData> orderItems) {

        private static OrderCreatedData of(Long userId, List<OrderItem> orderItems) {
            return new OrderCreatedData(
                userId,
                orderItems.stream().map(OrderItemData::from).collect(Collectors.toList())
            );
        }

    }
}
