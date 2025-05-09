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

public record OrderCanceledEvent(@NotNull String orderId, @NotBlank String eventId, @NotNull OrderEventType eventType,
                                 @NotNull OrderCanceledData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static OrderCanceledEvent from(OrderEntity orderEntity, Boolean isStockOccupied, Boolean isPaymentCompleted) {
        return new OrderCanceledEvent(
            orderEntity.getId(),
            UUID.randomUUID().toString(),
            OrderEventType.CANCELED,
            OrderCanceledData.of(
                orderEntity.getBuyerId(),
                orderEntity.getOrderItems(),
                isStockOccupied,
                isPaymentCompleted
            ),
            LocalDateTime.now()
        );
    }


    private record OrderCanceledData(@NotNull Long userId, @NotEmpty List<OrderItemData> orderItems,
                                     @NotNull Boolean isStockOccupied, @NotNull Boolean isPaymentCompleted) {

        private static OrderCanceledData of(Long userId, List<OrderItem> orderItems, Boolean isStockOccupied, Boolean isPaymentCompleted) {
            return new OrderCanceledData(
                userId,
                orderItems.stream().map(OrderItemData::from).collect(Collectors.toList()),
                isStockOccupied,
                isPaymentCompleted
            );
        }

    }
}
