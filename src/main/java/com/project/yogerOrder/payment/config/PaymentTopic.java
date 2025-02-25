package com.project.yogerOrder.payment.config;

import com.project.yogerOrder.payment.event.PaymentEventType;

public class PaymentTopic {
    public static final String COMPLETED = "yoger.payment.prd.completed";
    public static final String CANCELED = "yoger.payment.prd.canceled";
    public static final String ERRORED = "yoger.payment.prd.errored";

    public static String getTopicByEvent(PaymentEventType paymentEventType) {
        switch (paymentEventType) {
            case COMPLETED:
                return COMPLETED;
            case CANCELED:
                return CANCELED;
            case ERRORED:
                return ERRORED;
            default:
                throw new RuntimeException("Invalid event type");
        }
    }
}
