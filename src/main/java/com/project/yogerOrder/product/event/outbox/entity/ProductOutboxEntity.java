package com.project.yogerOrder.product.event.outbox.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import com.project.yogerOrder.global.util.outbox.entity.OutboxEntity;
import com.project.yogerOrder.product.event.ProductEventType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "product_outbox")
public class ProductOutboxEntity extends OutboxEntity {

    public ProductOutboxEntity(ProductEventType eventType, String payload) {
        super(eventType.toString(), payload);
    }
}
