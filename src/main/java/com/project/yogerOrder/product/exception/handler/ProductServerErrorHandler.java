package com.project.yogerOrder.product.exception.handler;

import com.project.yogerOrder.global.exception.specific.NotHandledException;
import com.project.yogerOrder.product.exception.ProductServerStateException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

import java.io.IOException;

@Component
public class ProductServerErrorHandler implements ErrorHandler {

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR))
            throw new ProductServerStateException();

        throw new NotHandledException();
    }
}
