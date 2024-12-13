package com.project.yogerOrder.payment.exception;

import com.project.yogerOrder.global.exception.specific.InvalidRequestException;

public class InvalidPaymentRequestException extends InvalidRequestException {

    public InvalidPaymentRequestException() {
        super("유효하지 않은 결제 요청입니다.");
    }
}
