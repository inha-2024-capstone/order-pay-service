package com.project.yogerOrder.order.entity;

import java.util.List;

public enum OrderState {
    CREATED, STOCK_CONFIRMED, PAYMENT_COMPLETED, COMPLETED, CANCELED, ERRORED;


    public static List<OrderState> getPayableStates() {
        return List.of(CREATED, STOCK_CONFIRMED);
    }


    public Boolean isStockOccupied() {
        return this == STOCK_CONFIRMED || this == COMPLETED;
    }

    public Boolean isPayable() {
        return this == CREATED || this == STOCK_CONFIRMED;
    }

    public Boolean isPaymentCompleted() {
        return this == PAYMENT_COMPLETED || this == COMPLETED;
    }
}
