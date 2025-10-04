package com.project.yogerOrder.order.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.project.yogerOrder.order.dto.request.OrderItemRequestDTO;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderCompletedEvent(@NotNull String orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                  @NotNull OrderCompletedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static OrderCompletedEvent from(OrderEntity orderEntity) {
        return new OrderCompletedEvent(
            orderEntity.getId(),
            UUID.randomUUID().toString(),
            OrderEventType.COMPLETED,
            OrderCompletedData.of(orderEntity.getBuyerId(), orderEntity.getOrderItems()),
            LocalDateTime.now()
        );
    }


    public Long getUserId() {
        return data().userId();
    }

    public List<OrderItemRequestDTO> getOrderItems() {
        return data().toDTOs();
    }


    private record OrderCompletedData(@NotNull Long userId, @NotEmpty List<OrderItemData> orderItems) {

        private static OrderCompletedData of(Long userId, List<OrderItem> orderItems) {
            return new OrderCompletedData(
                userId,
                orderItems.stream().map(OrderItemData::from).collect(Collectors.toList())
            );
        }


        private List<OrderItemRequestDTO> toDTOs() {
            return orderItems.stream().map(OrderItemRequestDTO::from).collect(Collectors.toList());
        }

    }

}
