package com.project.yogerOrder.product.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeStockRequestDTO(@NotNull Integer quantity) {
}
