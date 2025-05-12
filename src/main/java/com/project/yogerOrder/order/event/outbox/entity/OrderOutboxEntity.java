package com.project.yogerOrder.order.event.outbox.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import com.project.yogerOrder.global.util.outbox.entity.OutboxEntity;
import com.project.yogerOrder.order.event.OrderEventType;
import com.project.yogerOrder.order.event.config.OrderTopic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "order_outbox")
public class OrderOutboxEntity extends OutboxEntity {

    public OrderOutboxEntity(OrderEventType eventType, String payload) {
        super(OrderTopic.getTopicByEvent(eventType), payload);
    }
}
