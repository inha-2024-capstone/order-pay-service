package com.project.yogerOrder.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.yogerOrder.cart.entity.CartEntity;
import com.project.yogerOrder.global.util.db.ExcludeFromJpaRepository;

@ExcludeFromJpaRepository
public interface CartRepository extends MongoRepository<CartEntity, Long> {
}
