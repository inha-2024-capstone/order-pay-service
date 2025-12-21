package com.project.yogerOrder.product.exception;

import com.project.yogerOrder.global.exception.ExternalServiceException;

public class ProductServerStateException extends ExternalServiceException {

    public ProductServerStateException() {
        super("상품 서버 상태가 정상이 아닙니다.");
    }
}
