package com.project.yogerOrder.product.service;

import com.project.yogerOrder.product.config.ProductConfig;
import com.project.yogerOrder.product.dto.request.ChangeStockRequestDTO;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.handler.ProductClientErrorHandler;
import com.project.yogerOrder.product.exception.ProductInsufficientException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.handler.ProductServerErrorHandler;
import com.project.yogerOrder.product.exception.ProductServerStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

//TODO circuit breaker
@Service
public class ExternalProductService implements ProductService {

    private final RestClient restClient;

    // @ConfigurationProperties를 사용할 경우, 멤버 변수 선언과 동시에 초기화가 불가능함에 따라 생성자에서 초기화
    @Autowired
    public ExternalProductService(ProductConfig config,
                                  RestClient.Builder restClientBuilder, // 테스트하기 위해서 builder를 주입받아야 함
                                  ProductServerErrorHandler productServerErrorHandler,
                                  ProductClientErrorHandler productClientErrorHandler) {
        this.restClient = restClientBuilder
                .baseUrl(config.url())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, productClientErrorHandler)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, productServerErrorHandler)
                .build();
    }


    @Override
    public ProductResponseDTO findById(Long productId) throws ProductServerStateException, ProductNotFoundException {
        return restClient.get()
                .uri("/{productId}", productId)
                .retrieve()
                .body(ProductResponseDTO.class);
    }

    private void changeStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException, ProductInsufficientException {
        restClient.patch()
                .uri("/{productId}/stock/change", productId)
                .body(new ChangeStockRequestDTO(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void decreaseStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException, ProductInsufficientException {
        changeStock(productId, -quantity);
    }

    @Override
    public void increaseStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException {
        changeStock(productId, quantity);
    }
}
