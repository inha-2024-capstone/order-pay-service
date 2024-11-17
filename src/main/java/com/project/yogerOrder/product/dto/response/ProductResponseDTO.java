package com.project.yogerOrder.product.dto.response;

import jakarta.validation.constraints.NotNull;

public record ProductResponseDTO(@NotNull Long productId,
                                 @NotNull Integer confirmedPrice,
                                 @NotNull Integer originalMaxPrice) {
}
