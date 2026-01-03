package com.project.yogerOrder.product.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.product.cache.domain.entity.ProductCacheEntity;
import com.project.yogerOrder.product.cache.domain.service.ProductCacheService;
import com.project.yogerOrder.product.cache.lock.service.ProductLockService;
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

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	
	private final RestClient restClient;

	private final ProductCacheService productCacheService;
	
	private final ProductLockService productLockService;
	
	@Autowired
	public ProductServiceImpl(ProductRepository productRepository,
		ProductConfig config,
		RestClient.Builder restClientBuilder, // 테스트하기 위해서 builder를 주입받아야 함
		ProductClientErrorHandler productClientErrorHandler,
		ProductServerErrorHandler productServerErrorHandler,
	    ProductCacheService productCacheService,
		ProductLockService productLockService) {
		
		this.productRepository = productRepository;
		this.restClient = restClientBuilder
			.baseUrl(config.url())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultStatusHandler(HttpStatusCode::is4xxClientError, productClientErrorHandler)
			.defaultStatusHandler(HttpStatusCode::is5xxServerError, productServerErrorHandler)
			.build();
		this.productCacheService = productCacheService;
		this.productLockService = productLockService;
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
		// 최초 캐시 조회
		List<ProductCacheEntity> productCaches = productCacheService.findAllByIds(productIds);
		List<ProductResponseDTO> productResponseDTOs = productCaches.stream()
			.map(ProductResponseDTO::from)
			.collect(Collectors.toList()); // 수정가능하도록 반환

		// 캐시에 없는 상품 키 필터링
		List<Long> productKeysInDB = findKeysInDB(productIds, productCaches);
		
		// 캐시에 없는 상품이 있는 경우 잠금 획득 후 갱신 처리
		if (!productKeysInDB.isEmpty()) {
			List<ProductResponseDTO> refreshedProductResponseDTOs = productLockService.runWithDistributedLock(
				productKeysInDB,
				() -> {
					// Lock 획득 후 다시 캐시 확인
					List<ProductCacheEntity> refreshedProductCaches = productCacheService.findAllByIds(productKeysInDB);
					List<Long> refreshedKeysInDB = findKeysInDB(productKeysInDB, refreshedProductCaches);
					
					List<ProductResponseDTO> productResponseDTOs2 = refreshedProductCaches.stream()
						.map(ProductResponseDTO::from)
						.collect(Collectors.toList());
					
					// 캐시에 모두 존재하지 않는 경우
					if (!refreshedKeysInDB.isEmpty()) {
						// DB 조회
						List<ProductEntity> productEntities = productRepository.findAllById(refreshedKeysInDB);
						if (productEntities.size() != refreshedKeysInDB.size()) {
							throw new ProductNotFoundException();
						}
						
						// 캐시 저장
						productCacheService.saveAll(productEntities);
						
						productResponseDTOs2.addAll(productEntities.stream()
							.map(ProductResponseDTO::from)
							.toList()
						);
					}
					
					return productResponseDTOs2;
				}
			);
			
			productResponseDTOs.addAll(refreshedProductResponseDTOs);
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
