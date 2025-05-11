package com.project.yogerOrder.product.event;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDeductionFailedEvent(@NotBlank String eventId, @NotNull ProductEventType eventType,
                                          @NotNull ProductDeductionFailedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public static ProductDeductionFailedEvent of(String orderId) {
        return new ProductDeductionFailedEvent(
            UUID.randomUUID().toString(),
            ProductEventType.DEDUCTION_FAILED,
            ProductDeductionFailedData.of(orderId),
            LocalDateTime.now()
        );
    }

    public String getOrderId() {
        return data().orderId();
    }

    public record ProductDeductionFailedData(@NotNull String orderId) {

        private static ProductDeductionFailedData of(String orderId) {
            return new ProductDeductionFailedData(orderId);
        }

    }
}
