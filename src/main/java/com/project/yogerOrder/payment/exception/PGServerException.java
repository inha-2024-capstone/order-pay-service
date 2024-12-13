package com.project.yogerOrder.payment.exception;

import com.project.yogerOrder.global.exception.ExternalServiceException;

public class PGServerException extends ExternalServiceException {

    public PGServerException() {
        super("PG 서비스에서 에러가 발생했습니다.");
    }
}
