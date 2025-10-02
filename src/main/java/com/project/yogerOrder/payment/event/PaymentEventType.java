package com.project.yogerOrder.payment.event;

public enum PaymentEventType {
    COMPLETED, CANCELED, ERRORED;
    
    @Override
    public String toString() {
        return this.name().toUpperCase();
    }
}
