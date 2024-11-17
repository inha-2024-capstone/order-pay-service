package com.project.yogerOrder.global.exception.specific;

import com.project.yogerOrder.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
