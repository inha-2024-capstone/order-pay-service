package com.project.yogerOrder.order.exception;

import com.project.yogerOrder.global.exception.specific.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException() {
        super("주문 정보가 존재하지 않습니다.");
    }
}
