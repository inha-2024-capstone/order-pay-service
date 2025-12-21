package com.project.yogerOrder.product.exception.handler;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

import com.project.yogerOrder.global.exception.specific.UnHandledException;
import com.project.yogerOrder.product.exception.ProductInsufficientException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductClientErrorHandler implements ErrorHandler {

    @Override
    public void handle(@NotNull HttpRequest request, @NotNull ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode.isSameCodeAs(HttpStatus.CONFLICT)) {
            throw new ProductInsufficientException();
        } else if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            throw new ProductNotFoundException();
        }
        
        throw new UnHandledException();
    }
}