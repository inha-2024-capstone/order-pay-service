package com.project.yogerOrder.order.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.yogerOrder.global.util.db.ExcludeFromJpaRepository;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;

@ExcludeFromJpaRepository
public interface OrderRepository extends MongoRepository<OrderEntity, String> {

    List<OrderEntity> findAllByState(OrderState state);

    List<OrderEntity> findAllByBuyerIdAndState(Long buyerId, OrderState state);
}
