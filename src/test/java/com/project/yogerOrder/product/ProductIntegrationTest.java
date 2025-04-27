package com.project.yogerOrder.product;

import static org.mockito.BDDMockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.service.ExternalProductService;
import com.project.yogerOrder.product.service.ProductService;
import com.project.yogerOrder.product.util.cache.entity.ProductStockCache;
import com.project.yogerOrder.product.util.cache.service.ProductCacheService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ProductIntegrationTest extends UsingTestContainerTest {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductCacheService productCacheService;

	@MockBean
	private ExternalProductService externalProductService;

	@Test
	void findById() {
		// given
		Long productId = 1L;
		Integer price = 900;
		Integer stock = 10;

		ProductResponseDTO expected = new ProductResponseDTO(productId, price, stock);
		given(externalProductService.findById(productId)).willReturn(expected);

		// when
		ProductResponseDTO productResponseDTO = productService.findById(productId);

		// then
		Assertions.assertThat(productResponseDTO).usingRecursiveComparison().isEqualTo(expected);

		Optional<ProductStockCache> optionalProductStockCache = productCacheService.findById(productId);
		Assertions.assertThat(optionalProductStockCache).isPresent();

		ProductStockCache productStockCache = optionalProductStockCache.get();
		Assertions.assertThat(productStockCache.getProductId()).isEqualTo(productId);
		Assertions.assertThat(productStockCache.getPrice()).isEqualTo(price);
		Assertions.assertThat(productStockCache.getQuantity()).isEqualTo(stock);
	}

	@Test
	public void updateStock() {
		// given
		Long productId = 1L;
		Integer price = 900;
		Integer stock = 10;
		Integer updatedStock = 5;

		ProductResponseDTO expected = new ProductResponseDTO(productId, price, stock);
		given(externalProductService.findById(productId)).willReturn(expected);

		// when
		productService.updateStock(productId, updatedStock);

		Optional<ProductStockCache> optionalProductStockCache = productCacheService.findById(productId);
		Assertions.assertThat(optionalProductStockCache).isPresent();

		ProductStockCache productStockCache = optionalProductStockCache.get();
		Assertions.assertThat(productStockCache.getProductId()).isEqualTo(productId);
		Assertions.assertThat(productStockCache.getPrice()).isEqualTo(price);
		Assertions.assertThat(productStockCache.getQuantity()).isEqualTo(updatedStock);
	}

	@Test
	public void decreaseStock() {
		// given
		Long productId = 1L;
		Integer price = 900;
		Integer stock = 10;
		Integer decreasedStock = 5;

		ProductResponseDTO expected = new ProductResponseDTO(productId, price, stock);
		given(externalProductService.findById(productId)).willReturn(expected);

		// when
		productService.decreaseStock(productId, decreasedStock);

		Optional<ProductStockCache> optionalProductStockCache = productCacheService.findById(productId);
		Assertions.assertThat(optionalProductStockCache).isPresent();

		ProductStockCache productStockCache = optionalProductStockCache.get();
		Assertions.assertThat(productStockCache.getProductId()).isEqualTo(productId);
		Assertions.assertThat(productStockCache.getPrice()).isEqualTo(price);
		Assertions.assertThat(productStockCache.getQuantity()).isEqualTo(stock - decreasedStock);
	}
}
