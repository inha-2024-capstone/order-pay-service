package com.project.yogerOrder.product.cache.lock.service;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.project.yogerOrder.global.util.lock.multi.DistributedMultiLockHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductLockService {
	
	private static final String PRODUCT_LOCK_KEY_PREFIX = "product_lock:";
	
	private static final Long DEFAULT_WAIT_TIME_MILLIS = 5000L;
	
	private static final Long DEFAULT_LEASE_TIME_MILLIS = 10000L;
	
	
	private final DistributedMultiLockHandler distributedMultiLockHandler;
	
	public <T> T runWithDistributedLock(List<Long> productIds, Supplier<T> logic) {
		List<String> lockKeys = productIds.stream()
			.map(id -> PRODUCT_LOCK_KEY_PREFIX + id)
			.toList();
		
		return distributedMultiLockHandler.handleWithLock(
			lockKeys,
			DEFAULT_WAIT_TIME_MILLIS,
			DEFAULT_LEASE_TIME_MILLIS,
			logic
		);
	}
}
