package com.project.yogerOrder.order.event.outbox.entity;

import com.project.yogerOrder.global.util.outbox.entity.OutboxEntity;
import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.event.OrderEventType;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class OrderOutboxEntity extends OutboxEntity {

    public OrderOutboxEntity(OrderEventType eventType, String payload) {
        super(OrderTopic.getTopicByEvent(eventType), payload);
    }
}
