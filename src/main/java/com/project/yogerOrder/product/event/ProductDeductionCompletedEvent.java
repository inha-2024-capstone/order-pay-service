package com.project.yogerOrder.product.event;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDeductionCompletedEvent(@NotBlank String eventId, @NotNull ProductEventType eventType,
                                             @NotNull ProductDeductionCompletedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static ProductDeductionCompletedEvent of(String orderId) {
        return new ProductDeductionCompletedEvent(
            UUID.randomUUID().toString(),
            ProductEventType.DEDUCTION_COMPLETED,
            ProductDeductionCompletedData.of(orderId),
            LocalDateTime.now()
        );
    }


    public String getOrderId() {
        return data().orderId();
    }


    private record ProductDeductionCompletedData(@NotNull String orderId) {

        private static ProductDeductionCompletedData of(String orderId) {
            return new ProductDeductionCompletedData(orderId);
        }

    }
}
