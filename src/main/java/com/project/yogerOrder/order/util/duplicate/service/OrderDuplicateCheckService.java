package com.project.yogerOrder.order.util.duplicate.service;

import java.util.Collections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.project.yogerOrder.order.util.duplicate.exception.OrderDuplicatedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDuplicateCheckService {

	private static final String ORDER_REQUEST_ID_PREFIX = "orderRequestId:";

	private static final Long ORDER_REQUEST_ID_TTL = 1000L * 60 * 5; // 5분

	private final RedisTemplate<String, Object> redisTemplate;

	public void register(String orderRequestId) {
		Boolean isAbsent = putIfAbsentWithTTL(ORDER_REQUEST_ID_PREFIX + orderRequestId, "exist", ORDER_REQUEST_ID_TTL);

		if (Boolean.FALSE.equals(isAbsent)) {
			throw new OrderDuplicatedException();
		}
	}

	private Boolean putIfAbsentWithTTL(String key, String value, long ttlMillis) {
		String lua = "return redis.call('SET', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2])";
		DefaultRedisScript<String> script = new DefaultRedisScript<>(lua, String.class);
		String result = redisTemplate.execute(script, Collections.singletonList(key), value, String.valueOf(ttlMillis));
		// SET 성공 시 "OK" 반환, 실패 시 null
		return "OK".equals(result);
	}

}
