package com.project.yogerOrder.payment.exception;

import com.project.yogerOrder.global.exception.specific.InvalidRequestException;

public class InvalidPaymentRefundException extends InvalidRequestException {

    public InvalidPaymentRefundException() {
        super("유효하지 않은 결제 취소입니다.");
    }
}
