package com.project.yogerOrder.order.dto.response;

import jakarta.validation.constraints.NotBlank;

public record OrderResultResponseDTO(@NotBlank String orderId) {
}
