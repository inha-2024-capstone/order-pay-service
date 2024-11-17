package com.project.yogerOrder.global.exception.specific;

import com.project.yogerOrder.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidStateException extends BusinessException {

    public InvalidStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
