package com.project.yogerOrder.product.util.cache.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.util.cache.entity.ProductStockCache;
import com.project.yogerOrder.product.util.cache.repository.ProductStockCacheRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCacheService {

	private final ProductStockCacheRepository productStockCacheRepository;

	public ProductStockCache save(Long productId, Integer quantity, Integer price) {
		return productStockCacheRepository.save(new ProductStockCache(productId, quantity, price));
	}

	public Optional<ProductStockCache> findById(Long productId) {
		return productStockCacheRepository.findById(productId);
	}

	public void updateStock(Long productId, Integer quantity) throws ProductNotFoundException {
		ProductStockCache productStockCache = productStockCacheRepository.findById(productId)
			.orElseThrow(ProductNotFoundException::new);
		productStockCache = ProductStockCache.updateStock(productStockCache, quantity);

		productStockCacheRepository.save(productStockCache);
	}

	public void decreaseStock(Long productId, Integer quantity) {
		ProductStockCache productStockCache = productStockCacheRepository.findById(productId)
			.orElseThrow(ProductNotFoundException::new);
		productStockCache.decreaseStock(quantity);

		productStockCacheRepository.save(productStockCache);
	}
}
