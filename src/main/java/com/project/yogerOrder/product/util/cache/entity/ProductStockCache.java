package com.project.yogerOrder.product.util.cache.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.project.yogerOrder.product.exception.InsufficientStockException;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RedisHash(value = "productStockCache", timeToLive = 600)
public class ProductStockCache {

	@Id
	private Long productId;

	private Integer quantity;

	private Integer price;

	public void decreaseStock(@NotNull Integer quantity) throws InsufficientStockException {
		if (this.quantity < quantity) {
			throw new InsufficientStockException();
		}

		this.quantity -= quantity;
	}

	public static ProductStockCache updateStock(ProductStockCache productStockCache, Integer quantity) {
		return new ProductStockCache(productStockCache.getProductId(), quantity, productStockCache.getPrice());
	}


}
