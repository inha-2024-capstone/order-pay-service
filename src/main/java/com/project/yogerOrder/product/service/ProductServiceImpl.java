package com.project.yogerOrder.product.service;

import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.product.cache.entity.ProductCacheEntity;
import com.project.yogerOrder.product.cache.service.ProductCacheService;
import com.project.yogerOrder.product.config.ProductConfig;
import com.project.yogerOrder.product.dto.request.ReserveProductsRequestDTO;
import com.project.yogerOrder.product.dto.request.UpsertProductRequestDTO;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.entity.ProductEntity;
import com.project.yogerOrder.product.exception.ProductInsufficientException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.ProductServerStateException;
import com.project.yogerOrder.product.exception.handler.ProductClientErrorHandler;
import com.project.yogerOrder.product.exception.handler.ProductServerErrorHandler;
import com.project.yogerOrder.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	
	private final RestClient restClient;

	private final ProductCacheService productCacheService;
	
	@Autowired
	public ProductServiceImpl(ProductRepository productRepository,
		ProductConfig config,
		RestClient.Builder restClientBuilder, // 테스트하기 위해서 builder를 주입받아야 함
		ProductClientErrorHandler productClientErrorHandler,
		ProductServerErrorHandler productServerErrorHandler,
	    ProductCacheService productCacheService) {
		
		this.productRepository = productRepository;
		this.restClient = restClientBuilder
			.baseUrl(config.url())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultStatusHandler(HttpStatusCode::is4xxClientError, productClientErrorHandler)
			.defaultStatusHandler(HttpStatusCode::is5xxServerError, productServerErrorHandler)
			.build();
		this.productCacheService = productCacheService;
	}
	
	
	@Override
	public void reserveStocks(String orderId, List<OrderItem> orderItems) throws ProductServerStateException, ProductNotFoundException, ProductInsufficientException {
		restClient.patch()
			.uri("/stocks/reserve")
			.body(new ReserveProductsRequestDTO(orderId, orderItems))
			.retrieve()
			.toBodilessEntity();
	}
	
	@Override
	@Transactional
	public void upsertProduct(UpsertProductRequestDTO upsertProductRequestDTO) {
		productRepository.save(upsertProductRequestDTO.toEntity());
	}

	@Override
	public List<ProductResponseDTO> findByIds(List<Long> productIds) throws ProductNotFoundException {
		List<ProductCacheEntity> productCaches = productCacheService.findAllByIds(productIds);
		ArrayList<ProductResponseDTO> productResponseDTOs = new ArrayList<>(productCaches.stream().map(ProductResponseDTO::from).toList());

		List<Long> productKeysInDB = findKeysInDB(productIds, productCaches);
		if (!productKeysInDB.isEmpty()) {
			List<ProductEntity> productEntities = productRepository.findAllById(productKeysInDB);
			if (productEntities.size() != productKeysInDB.size()) {
				throw new ProductNotFoundException();
			}

			productResponseDTOs.addAll(productEntities.stream().map(ProductResponseDTO::from).toList());

			productCacheService.saveAll(productEntities);
		}

		return productResponseDTOs;
	}

	private List<Long> findKeysInDB(List<Long> productIds, List<ProductCacheEntity> productCaches) {
		Map<Long, ProductCacheEntity> productCacheMap = productCaches.stream().collect(Collectors.toMap(
				productCacheEntity -> Long.parseLong(productCacheEntity.getId()),
				productCacheEntity -> productCacheEntity
		));

		return productIds.stream().filter(productId -> !productCacheMap.containsKey(productId)).toList();
	}
}
