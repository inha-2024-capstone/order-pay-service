package com.project.yogerOrder.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponseDTO(@NotNull Long id,
                                 @NotNull Integer price,
                                 @NotNull Integer stock) {
}
