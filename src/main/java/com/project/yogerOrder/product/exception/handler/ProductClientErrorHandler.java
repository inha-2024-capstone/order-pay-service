package com.project.yogerOrder.product.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yogerOrder.global.exception.CustomExceptionEnum;
import com.project.yogerOrder.global.exception.ErrorResponse;
import com.project.yogerOrder.global.exception.specific.NotHandledException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ProductClientErrorHandler implements ErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(@NotNull HttpRequest request, @NotNull ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            throw new ProductNotFoundException();
        }


        ErrorResponse errorResponse = objectMapper.readValue(readResponseBody(response), ErrorResponse.class);

        try {
            throw CustomExceptionEnum.getByCode(errorResponse.getCode())
                    .getExceptionClass()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new NotHandledException(e);
        }
    }

    private String readResponseBody(ClientHttpResponse response) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        }
    }
}
