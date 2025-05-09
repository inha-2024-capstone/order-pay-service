package com.project.yogerOrder.product.event;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductUpdatedEvent(@NotNull Long productId, @NotBlank String eventId, @NotNull ProductEventType eventType,
								  @NotNull ProductUpdatedData data, @NotNull LocalDateTime occurrenceDateTime) {

	public static ProductUpdatedEvent of(Long productId, String name, Integer stock, Integer price) {
		return new ProductUpdatedEvent(
				productId,
				UUID.randomUUID().toString(),
				ProductEventType.UPDATED,
				new ProductUpdatedData(name, stock, price),
				LocalDateTime.now()
		);
	}

    private record ProductUpdatedData(@NotBlank String name, @NotNull Integer stock, @NotNull Integer price) {
    }
}
