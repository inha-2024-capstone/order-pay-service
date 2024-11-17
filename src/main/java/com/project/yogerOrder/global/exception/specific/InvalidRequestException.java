package com.project.yogerOrder.global.exception.specific;

import com.project.yogerOrder.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
