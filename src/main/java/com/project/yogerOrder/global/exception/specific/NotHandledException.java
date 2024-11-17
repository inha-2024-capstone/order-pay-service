package com.project.yogerOrder.global.exception.specific;

import com.project.yogerOrder.global.exception.CustomRuntimeException;
import org.springframework.http.HttpStatus;

public class NotHandledException extends CustomRuntimeException {

    private static final String DEFAULT_MESSAGE = "처리되지 않은 에러입니다.";
    private static final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public NotHandledException() {
        super(DEFAULT_MESSAGE);
    }

    public NotHandledException(Throwable throwable) {
        super(throwable, DEFAULT_HTTP_STATUS, throwable.getMessage());
    }
}
