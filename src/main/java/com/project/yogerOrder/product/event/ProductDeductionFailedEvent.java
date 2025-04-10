package com.project.yogerOrder.product.event;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDeductionFailedEvent(@NotNull Long productId, @NotBlank String eventId, @NotNull ProductEventType eventType,
                                          @NotNull ProductDeductionFailedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public record ProductDeductionFailedData(@NotNull Long orderId, @NotNull Integer orderQuantity, @NotNull Integer currentStock) {
    }
}
