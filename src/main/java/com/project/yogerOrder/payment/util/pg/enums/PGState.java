package com.project.yogerOrder.payment.util.pg.enums;

public enum PGState {
    READY, PAID, FAILED;

    public Boolean isPaid() {
        return this == PAID;
    }
}
