package com.project.yogerOrder.order.util.duplicate.service;

import org.springframework.stereotype.Service;

import com.project.yogerOrder.order.util.duplicate.entity.OrderDuplicateCheckEntity;
import com.project.yogerOrder.order.util.duplicate.exception.OrderDuplicatedException;
import com.project.yogerOrder.order.util.duplicate.repository.OrderDuplicateCheckRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDuplicateCheckService {

	private final OrderDuplicateCheckRepository orderDuplicateCheckRepository;

	public void register(Long orderRequestId) {
		if (orderDuplicateCheckRepository.findById(String.valueOf(orderRequestId)).isPresent()) {
			throw new OrderDuplicatedException();
		}

		orderDuplicateCheckRepository.save(new OrderDuplicateCheckEntity(orderRequestId));
	}

}
