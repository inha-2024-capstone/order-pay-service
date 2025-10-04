package com.project.yogerOrder.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomRuntimeException extends RuntimeException {

    protected final HttpStatus httpStatus;

    protected final String message;

    protected CustomRuntimeException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    protected CustomRuntimeException(String message) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    protected CustomRuntimeException(Throwable cause, HttpStatus httpStatus, String message) {
        super(cause);
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
