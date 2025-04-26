package com.project.yogerOrder.product.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.yogerOrder.global.util.lock.DistributedLock;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.InsufficientStockException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.ProductServerStateException;
import com.project.yogerOrder.product.util.cache.entity.ProductStockCache;
import com.project.yogerOrder.product.util.cache.service.ProductCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ExternalProductService externalProductService;

	private final ProductCacheService productCacheService;

	@Override
	@DistributedLock(category = "productCache", key = "#productId")
	public ProductResponseDTO findById(Long productId) throws ProductServerStateException, ProductNotFoundException {
		Optional<ProductStockCache> optionalProductStockCache = productCacheService.findById(productId);

		if (optionalProductStockCache.isPresent()) {
			ProductStockCache productStockCache = optionalProductStockCache.get();

			return new ProductResponseDTO(
				productStockCache.getProductId(),
				productStockCache.getQuantity(),
				productStockCache.getPrice()
			);
		}
		else {
			ProductResponseDTO productResponseDTO = externalProductService.findById(productId);

			productCacheService.save(
				productResponseDTO.id(),
				productResponseDTO.stock(),
				productResponseDTO.price()
			);

			return productResponseDTO;
		}
	}

	@Override
	@DistributedLock(category = "productCache", key = "#productId")
	public void updateStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException {
		try {
			productCacheService.updateStock(productId, quantity);
		} catch (ProductNotFoundException e) {
			ProductResponseDTO productResponseDTO = externalProductService.findById(productId);

			productCacheService.save(
				productResponseDTO.id(),
				productResponseDTO.stock(),
				productResponseDTO.price()
			);

			productCacheService.updateStock(productId, quantity);
		}
	}

	@Override
	@DistributedLock(category = "productCache", key = "#productId")
	public void decreaseStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException,
		InsufficientStockException {
		try {
			productCacheService.decreaseStock(productId, quantity);
		} catch (ProductNotFoundException e) {
			ProductResponseDTO productResponseDTO = externalProductService.findById(productId);

			productCacheService.save(
				productResponseDTO.id(),
				productResponseDTO.stock(),
				productResponseDTO.price()
			);

			productCacheService.decreaseStock(productId, quantity);
		}
	}
}
