package com.project.yogerOrder.product.cache.domain.repository;

import com.project.yogerOrder.global.util.db.ExcludeFromJpaRepository;
import com.project.yogerOrder.product.cache.domain.entity.ProductCacheEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ExcludeFromJpaRepository
public interface ProductCacheRepository extends CrudRepository<ProductCacheEntity, String> {

    @Override
    List<ProductCacheEntity> findAllById(Iterable<String> productIds);
}
