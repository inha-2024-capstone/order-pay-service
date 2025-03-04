package com.project.yogerOrder.order.util.stateMachine;

public enum OrderStateChangeEvent {
    STOCK_DEDUCTED, STOCK_DEDUCT_FAILED, PAID, PAYMENT_CANCELED, EXPIRED, ERRORED;
}
