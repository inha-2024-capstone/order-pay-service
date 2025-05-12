package com.project.yogerOrder.order.event.outbox.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yogerOrder.global.util.db.MongoTransactional;
import com.project.yogerOrder.order.event.OrderEventType;
import com.project.yogerOrder.order.event.outbox.entity.OrderOutboxEntity;
import com.project.yogerOrder.order.event.outbox.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@MongoTransactional(propagation = Propagation.MANDATORY)
public class OrderOutboxService {

    private final OrderOutboxRepository orderOutboxRepository;

    private final ObjectMapper objectMapper;

    public void saveOutbox(OrderEventType eventType, Object payload) {
        try {
            String stringPayload = objectMapper.writeValueAsString(payload);
            orderOutboxRepository.save(new OrderOutboxEntity(eventType, stringPayload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
