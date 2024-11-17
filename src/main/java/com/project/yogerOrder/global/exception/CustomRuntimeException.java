package com.project.yogerOrder.global.exception;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomRuntimeException extends RuntimeException {

    protected Integer code;

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

    @PostConstruct
    public void init() {
        Class<? extends CustomRuntimeException> aClass = this.getClass();
        CustomExceptionEnum byExceptionClass = CustomExceptionEnum.getByExceptionClass(aClass);

        this.code = byExceptionClass.getCode();
    }
}
