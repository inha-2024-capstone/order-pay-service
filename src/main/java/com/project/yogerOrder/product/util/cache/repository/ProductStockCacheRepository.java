package com.project.yogerOrder.product.util.cache.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.project.yogerOrder.product.util.cache.entity.ProductStockCache;

@Repository
public interface ProductStockCacheRepository extends CrudRepository<ProductStockCache, Long> {
}
