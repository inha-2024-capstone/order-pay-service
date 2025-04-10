package com.project.yogerOrder.order.util.duplicate.exception;

import org.springframework.http.HttpStatus;

import com.project.yogerOrder.global.exception.BusinessException;

public class OrderDuplicatedException extends BusinessException {
	public OrderDuplicatedException() {
		super(HttpStatus.CONFLICT, "중복된 주문 요청입니다.");
	}
}
