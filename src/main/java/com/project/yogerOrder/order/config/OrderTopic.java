package com.project.yogerOrder.order.config;

import com.project.yogerOrder.order.event.OrderEventType;

public class OrderTopic {
    public static final String CREATED = "yoger.order.prd.created";
    public static final String COMPLETED = "yoger.order.prd.completed";
    public static final String CANCELED = "yoger.order.prd.canceled";
    public static final String ERRORED = "yoger.order.prd.errored";


    public static String getTopicByEvent(OrderEventType orderEventType) {
        return switch (orderEventType) {
            case CREATED -> CREATED;
            case COMPLETED -> COMPLETED;
            case CANCELED -> CANCELED;
            case ERRORED -> ERRORED;
        };
    }
}
