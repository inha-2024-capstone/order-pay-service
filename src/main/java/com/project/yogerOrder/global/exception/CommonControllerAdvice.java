package com.project.yogerOrder.global.exception;

import com.project.yogerOrder.global.exception.specific.NotHandledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDefaultException(CustomRuntimeException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getCode(), e.getMessage()), e.getHttpStatus());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnHandledException(Exception e) {
        log.error("unhandled exception occurred", e);
        NotHandledException ex = new NotHandledException();
        return new ResponseEntity<>(new ErrorResponse(ex.getCode(), ex.getMessage()), ex.getHttpStatus());
    }

}
