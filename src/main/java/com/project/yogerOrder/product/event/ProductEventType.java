package com.project.yogerOrder.product.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProductEventType {
    @JsonProperty("deductionCompleted") DEDUCTION_COMPLETED,
    @JsonProperty("deductionFailed") DEDUCTION_FAILED,
    @JsonProperty("updated") UPDATED,
    @JsonProperty("confirm_reservation") CONFIRM_RESERVATION;
    
    public String toString() {
        return this.name().toUpperCase();
    }
}
