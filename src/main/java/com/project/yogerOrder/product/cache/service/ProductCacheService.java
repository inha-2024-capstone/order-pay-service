package com.project.yogerOrder.product.cache.service;

import com.project.yogerOrder.product.cache.entity.ProductCacheEntity;
import com.project.yogerOrder.product.cache.repository.ProductCacheRepository;
import com.project.yogerOrder.product.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final ProductCacheRepository productCacheRepository;


    public void saveAll(List<ProductEntity> productEntities) {
        productCacheRepository.saveAll(productEntities.stream().map(ProductCacheEntity::from).toList());
    }

    public List<ProductCacheEntity> findAllByIds(List<Long> productIds){
        return productCacheRepository.findAllById(productIds.stream().map(Object::toString).toList());
    }
}
