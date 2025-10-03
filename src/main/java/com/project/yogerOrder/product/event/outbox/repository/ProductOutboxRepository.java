package com.project.yogerOrder.product.event.outbox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.yogerOrder.global.util.db.ExcludeFromJpaRepository;
import com.project.yogerOrder.product.event.outbox.entity.ProductOutboxEntity;

@ExcludeFromJpaRepository
public interface ProductOutboxRepository extends MongoRepository<ProductOutboxEntity, String> {
}
