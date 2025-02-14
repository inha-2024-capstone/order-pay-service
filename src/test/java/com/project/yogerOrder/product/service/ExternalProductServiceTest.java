package com.project.yogerOrder.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yogerOrder.global.exception.CustomExceptionEnum;
import com.project.yogerOrder.product.config.ProductConfig;
import com.project.yogerOrder.product.dto.response.PriceByQuantity;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.handler.ProductClientErrorHandler;
import com.project.yogerOrder.product.exception.handler.ProductServerErrorHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(value = {
        ProductConfig.class,
        ProductService.class,
        CustomExceptionEnum.class,
        ExternalProductService.class,
        ProductClientErrorHandler.class,
        ProductServerErrorHandler.class
})
@ActiveProfiles("test")
class ExternalProductServiceTest {

    @Autowired
    private ProductConfig config;

    @Autowired
    private ProductService productService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findById() throws JsonProcessingException {
        // given
        Long productId = 1L;
        Integer confirmedPrice = 900;

        PriceByQuantity priceByQuantity = new PriceByQuantity(3, confirmedPrice);
        ProductResponseDTO expected = new ProductResponseDTO(productId, List.of(priceByQuantity));

        // when
        mockServer.expect(requestTo(config.url() + "/" + productId)).andRespond(withSuccess(
                objectMapper.writeValueAsString(expected),
                MediaType.APPLICATION_JSON
        ));

        ProductResponseDTO productResponseDTO = productService.findById(productId);

        // then
        Assertions.assertThat(productResponseDTO).usingRecursiveComparison().isEqualTo(expected);
    }
}