package com.project.yogerOrder.product.exception;

import com.project.yogerOrder.global.exception.specific.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException() {
        super("상품이 존재하지 않습니다.");
    }
}
