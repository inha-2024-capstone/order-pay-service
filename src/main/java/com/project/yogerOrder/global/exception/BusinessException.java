package com.project.yogerOrder.global.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends CustomRuntimeException {

    public BusinessException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
