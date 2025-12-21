package com.project.yogerOrder.order.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ConfirmReservationsRequestDTO(
	@NotBlank String orderId,
	@NotNull Long buyerId,
	@NotEmpty List<OrderItemRequestDTO> orderItemRequestDTOs) {
}
