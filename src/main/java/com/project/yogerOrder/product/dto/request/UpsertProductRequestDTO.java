package com.project.yogerOrder.product.dto.request;

import com.project.yogerOrder.product.entity.ProductEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertProductRequestDTO(@NotNull Long productId, @NotBlank String name, @NotNull Integer stock, @NotNull Integer price) {

	public ProductEntity toEntity() {
		return new ProductEntity(this.productId, this.name, this.stock, this.price);
	}
}
