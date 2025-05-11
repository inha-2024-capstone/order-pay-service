package com.project.yogerOrder.order.event.outbox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.yogerOrder.global.util.db.ExcludeFromJpaRepository;
import com.project.yogerOrder.order.event.outbox.entity.OrderOutboxEntity;

@ExcludeFromJpaRepository
public interface OrderOutboxRepository extends MongoRepository<OrderOutboxEntity, String> {
}
