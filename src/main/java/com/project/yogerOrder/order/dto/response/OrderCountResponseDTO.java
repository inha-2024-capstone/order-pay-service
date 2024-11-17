package com.project.yogerOrder.order.dto.response;

import jakarta.validation.constraints.NotNull;

public record OrderCountResponseDTO(@NotNull Integer count) {
}
