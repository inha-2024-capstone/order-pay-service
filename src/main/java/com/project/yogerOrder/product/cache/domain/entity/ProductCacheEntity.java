package com.project.yogerOrder.product.cache.domain.entity;

import com.project.yogerOrder.product.entity.ProductEntity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "productCache", timeToLive = 600)
public class ProductCacheEntity {

    @Id
    private String id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    @Min(0)
    private Integer price;

    private ProductCacheEntity(String id, String name, Integer stock, Integer price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static ProductCacheEntity from(ProductEntity productEntity) {
        return new ProductCacheEntity(
                productEntity.getId().toString(),
                productEntity.getName(),
                productEntity.getStock(),
                productEntity.getPrice()
        );
    }
}
