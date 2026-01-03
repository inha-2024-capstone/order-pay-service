package com.project.yogerOrder.global.util.lock.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DistributedMultiLockHandler {
	
	private final RedissonClient redissonClient;
	
	
	public <T> T handleWithLock(List<String> keys, long waitTimeInMilliSeconds, long leaseTimeInMilliSeconds, Supplier<T> logic) {
		// 변경 가능한 리스트로 복사
		keys = new ArrayList<>(keys);
		// 데드락 방지를 위해 키를 정렬
		Collections.sort(keys);
		
		RLock[] rLocks = keys.stream()
			.map(redissonClient::getLock)
			.toArray(RLock[]::new);
		
		RLock multiLock = redissonClient.getMultiLock(rLocks);
		
		try {
			if (multiLock.tryLock(waitTimeInMilliSeconds, leaseTimeInMilliSeconds, TimeUnit.MILLISECONDS)) {
				return logic.get();
			} else {
				throw new IllegalMonitorStateException("Failed to acquire multi lock within wait time for keys: " + keys);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Lock acquisition was interrupted.", e);
		} finally {
			if (multiLock.isHeldByCurrentThread()) {
				multiLock.unlock();
			}
		}
	}
}
