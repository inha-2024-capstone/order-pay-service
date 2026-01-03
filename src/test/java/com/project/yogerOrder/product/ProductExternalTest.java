package com.project.yogerOrder.product;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yogerOrder.global.exception.ErrorResponse;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.product.cache.domain.service.ProductCacheService;
import com.project.yogerOrder.product.cache.lock.service.ProductLockService;
import com.project.yogerOrder.product.config.ProductConfig;
import com.project.yogerOrder.product.exception.ProductInsufficientException;
import com.project.yogerOrder.product.exception.handler.ProductClientErrorHandler;
import com.project.yogerOrder.product.exception.handler.ProductServerErrorHandler;
import com.project.yogerOrder.product.repository.ProductRepository;
import com.project.yogerOrder.product.service.ProductService;
import com.project.yogerOrder.product.service.ProductServiceImpl;

@RestClientTest(value = {
	ProductConfig.class,
	ProductService.class,
	ProductServiceImpl.class,
	ProductClientErrorHandler.class,
	ProductServerErrorHandler.class
})
public class ProductExternalTest {
	
	@MockitoBean
	private ProductConfig config;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private MockRestServiceServer mockServer;
	
	@MockitoBean
	private ProductRepository productRepository;
	
	@MockitoBean
	private ProductCacheService productCacheService;
	
	@MockitoBean
	private ProductLockService productLockService;
	
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	
	@Test
	void decreaseStockNotFoundError() throws JsonProcessingException {
		// given
		String orderId = UUID.randomUUID().toString();
		List<OrderItem> orderItems = List.of(new OrderItem(1L, 2), new OrderItem(2L, 3));
		
		String url = "/stocks/reserve";
		
		mockServer.expect(requestTo(url)).andRespond(
			withRequestConflict()
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(new ErrorResponse("error")))
		);
		
		
		// when, then
		Assertions.assertThatThrownBy(() -> productService.reserveStocks(orderId, orderItems))
			.isInstanceOf(ProductInsufficientException.class);
	}
}
