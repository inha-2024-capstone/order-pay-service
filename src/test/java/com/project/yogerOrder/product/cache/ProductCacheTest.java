package com.project.yogerOrder.product.cache;

import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.product.cache.domain.entity.ProductCacheEntity;
import com.project.yogerOrder.product.cache.domain.service.ProductCacheService;
import com.project.yogerOrder.product.entity.ProductEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ProductCacheTest extends UsingTestContainerTest {

    @Autowired
    ProductCacheService productCacheService;


    @Test
    void basicCacheTest() {
        List<ProductEntity> productEntities = generateProductEntities();

        productCacheService.saveAll(productEntities);

        List<ProductCacheEntity> allCaches = productCacheService.findAllByIds(productEntities.stream().map(ProductEntity::getId).toList());

        Assertions.assertThat(allCaches).hasSize(productEntities.size());
    }

    private List<ProductEntity> generateProductEntities() {
        String name = "testProductName";

        return IntStream.range(1, 6).mapToObj(
                i -> new ProductEntity(
                        Long.parseLong(String.valueOf(i)),
                        name + i,
                        100 * i,
                        1000 * i
                )
        ).toList();
    }


}
