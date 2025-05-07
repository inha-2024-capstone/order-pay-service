package com.project.yogerOrder.product.event;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductUpdatedEvent(@NotNull Long productId, @NotBlank String eventId, @NotNull ProductEventType eventType,
								  @NotNull ProductUpdatedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public record ProductUpdatedData(@NotBlank String name, @NotNull Integer stock, @NotNull Integer price) {
    }
}
