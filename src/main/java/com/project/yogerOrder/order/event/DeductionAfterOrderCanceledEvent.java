package com.project.yogerOrder.order.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.project.yogerOrder.order.entity.OrderEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeductionAfterOrderCanceledEvent(@NotNull Long orderId, @NotBlank String eventId, @NotNull OrderEventType eventType,
                                               @NotNull OrderDeductionAfterCanceledData data, @NotNull LocalDateTime occurrenceDateTime) {

    private record OrderDeductionAfterCanceledData(@NotNull Long userId, @NotNull Long productId, @NotNull Integer orderQuantity) {
    }

    public static DeductionAfterOrderCanceledEvent from(OrderEntity orderEntity) {
        return new DeductionAfterOrderCanceledEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.DEDUCTION_AFTER_CANCELED,
                new OrderDeductionAfterCanceledData(
                        orderEntity.getBuyerId(),
                        orderEntity.getProductId(),
                        orderEntity.getQuantity()
                ),
                LocalDateTime.now()
        );
    }
}
