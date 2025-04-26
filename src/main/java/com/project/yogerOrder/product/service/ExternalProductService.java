package com.project.yogerOrder.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.project.yogerOrder.product.config.ProductConfig;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.ProductServerStateException;
import com.project.yogerOrder.product.exception.handler.ProductClientErrorHandler;
import com.project.yogerOrder.product.exception.handler.ProductServerErrorHandler;

@Service
public class ExternalProductService {

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


    public ProductResponseDTO findById(Long productId) throws ProductServerStateException, ProductNotFoundException {
        return restClient.get()
                .uri("/{productId}", productId)
                .retrieve()
                .body(ProductResponseDTO.class);
    }
}
