package com.project.yogerOrder.product.exception;

import org.springframework.http.HttpStatus;

import com.project.yogerOrder.global.exception.BusinessException;

public class ProductInsufficientException extends BusinessException {

    public ProductInsufficientException() {
        super(HttpStatus.CONFLICT, "물품 개수가 부족합니다.");
    }
}
