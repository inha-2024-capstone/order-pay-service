package com.project.yogerOrder.payment.exception;

import com.project.yogerOrder.global.exception.specific.InvalidStateException;

public class InvalidPaymentStateException extends InvalidStateException {

    public InvalidPaymentStateException() {
        super("결제 상태가 유효하지 않습니다.");
    }
}
