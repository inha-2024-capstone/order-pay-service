package com.project.yogerOrder.order.exception;

import com.project.yogerOrder.global.exception.RepositoryException;

public class OrderRepositoryException extends RepositoryException {
    public OrderRepositoryException() {
        super("주문 서비스 Repository에서 에러가 발생했습니다.");
    }
}
