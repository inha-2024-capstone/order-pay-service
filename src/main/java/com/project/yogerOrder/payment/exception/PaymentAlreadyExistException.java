package com.project.yogerOrder.payment.exception;

import com.project.yogerOrder.global.exception.specific.AlreadyExistException;

public class PaymentAlreadyExistException extends AlreadyExistException {

    public PaymentAlreadyExistException() {
        super("Payment already exist");
    }
}
