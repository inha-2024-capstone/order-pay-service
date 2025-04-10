package com.project.yogerOrder.order.util.duplicate.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RedisHash(value = "orderDuplicateCheck", timeToLive = 60 * 60 * 24)
public class OrderDuplicateCheckEntity {

	@Id
	private String id;
}
