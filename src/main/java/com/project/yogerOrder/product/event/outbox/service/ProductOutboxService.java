package com.project.yogerOrder.product.event.outbox.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yogerOrder.global.util.db.MongoTransactional;
import com.project.yogerOrder.product.event.ProductEventType;
import com.project.yogerOrder.product.event.outbox.entity.ProductOutboxEntity;
import com.project.yogerOrder.product.event.outbox.repository.ProductOutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@MongoTransactional(propagation = Propagation.MANDATORY)
public class ProductOutboxService {

    private final ProductOutboxRepository productOutboxRepository;

    private final ObjectMapper objectMapper;

    public void saveOutbox(ProductEventType eventType, Object payload) {
        try {
            String stringPayload = objectMapper.writeValueAsString(payload);
            productOutboxRepository.save(new ProductOutboxEntity(eventType, stringPayload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
