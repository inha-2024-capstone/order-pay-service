package com.project.yogerOrder.product.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.event.OrderItemData;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ConfirmProductReservationEvent(@NotNull String orderId, @NotBlank String eventId, @NotBlank ProductEventType eventType,
                                             @NotNull ConfirmProductReservationData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static ConfirmProductReservationEvent of(String orderId, Long buyerId, List<OrderItem> orderItems) {
        return new ConfirmProductReservationEvent(
                orderId,
                UUID.randomUUID().toString(),
                ProductEventType.CONFIRM_RESERVATION,
                ConfirmProductReservationData.of(buyerId, orderItems),
                LocalDateTime.now()
        );
    }

    private record ConfirmProductReservationData(@NotNull Long userId, @NotEmpty List<OrderItemData> orderItems) {

        private static ConfirmProductReservationData of(Long userId, List<OrderItem> orderItems) {
            return new ConfirmProductReservationData(
                userId,
                orderItems.stream().map(OrderItemData::from).collect(Collectors.toList())
            );
        }

    }
}
