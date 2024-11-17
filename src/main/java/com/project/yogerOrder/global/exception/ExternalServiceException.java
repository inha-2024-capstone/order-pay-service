package com.project.yogerOrder.global.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends CustomRuntimeException {

    public ExternalServiceException() {
        this("외부 서비스에서 에러가 발생했습니다.");
    }

    public ExternalServiceException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
