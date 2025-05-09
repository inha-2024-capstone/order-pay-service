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

public record PaymentCompletedAfterOrderCanceledEvent(@NotNull String orderId,
                                                      @NotBlank String eventId,
                                                      @NotNull OrderDeductionAfterCanceledData data,
                                                      @NotNull LocalDateTime occurrenceDateTime) {

    public static PaymentCompletedAfterOrderCanceledEvent from(OrderEntity orderEntity) {
        return new PaymentCompletedAfterOrderCanceledEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderDeductionAfterCanceledData.of(orderEntity.getBuyerId(), orderEntity.getOrderItems()),
                LocalDateTime.now()
        );
    }


    private record OrderDeductionAfterCanceledData(@NotNull Long userId, @NotEmpty List<OrderItemData> orderItems) {

        private static OrderDeductionAfterCanceledData of(Long userId, List<OrderItem> orderItems) {
            return new OrderDeductionAfterCanceledData(
                userId,
                orderItems.stream().map(OrderItemData::from).collect(Collectors.toList())
            );
        }

    }
}
