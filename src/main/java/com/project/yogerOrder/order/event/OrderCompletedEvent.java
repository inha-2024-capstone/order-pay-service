package com.project.yogerOrder.order.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.project.yogerOrder.order.entity.OrderEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderCompletedEvent(@NotNull Long orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                  @NotNull OrderCompletedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public record OrderCompletedData(@NotNull Long userId, @NotEmpty List<OrderCompletedProductData> products) {
    }

    public record OrderCompletedProductData(@NotNull Long productId, @NotNull Integer orderQuantity) {
    }

    public static OrderCompletedEvent from(OrderEntity orderEntity) {
        return new OrderCompletedEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.COMPLETED,
                new OrderCompletedData(
                    orderEntity.getBuyerId(),
                    List.of(new OrderCompletedProductData(orderEntity.getProductId(), orderEntity.getQuantity()))
                ),
                LocalDateTime.now()
        );
    }

    public Long getUserId() {
        return data().userId();
    }

    public List<Map.Entry<Long, Integer>> getProducts() {
        return data().products().stream()
                .map(product -> Map.entry(product.productId(), product.orderQuantity()))
                .toList();
    }

}
