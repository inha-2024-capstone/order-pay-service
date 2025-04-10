package com.project.yogerOrder.order.util.duplicate.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.project.yogerOrder.order.util.duplicate.entity.OrderDuplicateCheckEntity;

@Repository
public interface OrderDuplicateCheckRepository extends CrudRepository<OrderDuplicateCheckEntity, String> {
}
