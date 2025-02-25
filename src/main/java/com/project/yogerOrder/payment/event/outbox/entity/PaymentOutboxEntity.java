package com.project.yogerOrder.payment.event.outbox.entity;

import com.project.yogerOrder.global.util.outbox.entity.OutboxEntity;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentEventType;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PaymentOutboxEntity extends OutboxEntity {

    public PaymentOutboxEntity(PaymentEventType eventType, String payload) {
        super(PaymentTopic.getTopicByEvent(eventType), payload);
    }
}
