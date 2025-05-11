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

public record OrderErroredEvent(@NotNull String orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                @NotNull OrderErroredData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static OrderErroredEvent from(OrderEntity orderEntity) {
        return new OrderErroredEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.ERRORED,
                OrderErroredData.of(orderEntity.getBuyerId(), orderEntity.getOrderItems()),
                LocalDateTime.now()
        );
    }

    private record OrderErroredData(@NotNull Long userId, @NotEmpty List<OrderItemData> orderItems) {

        private static OrderErroredData of(Long userId, List<OrderItem> orderItems) {
            return new OrderErroredData(
                userId,
                orderItems.stream().map(OrderItemData::from).collect(Collectors.toList())
            );
        }

    }
}
