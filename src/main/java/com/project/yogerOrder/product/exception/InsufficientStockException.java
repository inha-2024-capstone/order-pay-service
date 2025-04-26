package com.project.yogerOrder.product.exception;

import org.springframework.http.HttpStatus;

import com.project.yogerOrder.global.exception.BusinessException;

public class InsufficientStockException extends BusinessException {
	public InsufficientStockException() {
		super(HttpStatus.CONFLICT, "재고가 부족합니다.");
	}
}
