package com.project.yogerOrder.product.event.config;

import com.project.yogerOrder.product.event.ProductEventType;

public class ProductTopic {
    public static final String DEDUCTION_COMPLETED = "yoger.product.prd.deductionCompleted";
    public static final String DEDUCTION_FAILED = "yoger.product.prd.deductionFailed";
    public static final String UPDATED = "yoger.product.prd.updated";
    public static final String CONFIRM_RESERVATION = "yoger.product.prd.confirmReservation";
    
    public static String getTopicByEventType(ProductEventType productEventType) {
        return switch (productEventType) {
            case DEDUCTION_COMPLETED -> DEDUCTION_COMPLETED;
            case DEDUCTION_FAILED -> DEDUCTION_FAILED;
            case UPDATED -> UPDATED;
            case CONFIRM_RESERVATION -> CONFIRM_RESERVATION;
        };
    }
}
