package com.project.yogerOrder.global.exception.specific;

import com.project.yogerOrder.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyExistException extends BusinessException {

    public AlreadyExistException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
