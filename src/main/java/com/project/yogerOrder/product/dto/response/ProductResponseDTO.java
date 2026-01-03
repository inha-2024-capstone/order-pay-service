package com.project.yogerOrder.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.yogerOrder.product.cache.domain.entity.ProductCacheEntity;
import com.project.yogerOrder.product.entity.ProductEntity;

import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponseDTO(
    @NotNull Long id,
    @NotNull Integer price,
    @NotNull Integer stock) {

    public static ProductResponseDTO from(@NotNull ProductEntity productEntity) {
        return new ProductResponseDTO(productEntity.getId(), productEntity.getPrice(), productEntity.getStock());
    }

    public static ProductResponseDTO from(@NotNull ProductCacheEntity productCacheEntity) {
        return new ProductResponseDTO(
                Long.parseLong(productCacheEntity.getId()),
                productCacheEntity.getPrice(),
                productCacheEntity.getStock()
        );
    }
}
