package com.project.yogerOrder.global.exception;

import com.project.yogerOrder.global.exception.specific.UnHandledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDefaultException(CustomRuntimeException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), e.getHttpStatus());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnHandledException(Exception e) {
        log.error("unhandled exception occurred", e);
        UnHandledException unHandledException = new UnHandledException();
        return new ResponseEntity<>(new ErrorResponse(unHandledException.getMessage()), unHandledException.getHttpStatus());
    }

}
