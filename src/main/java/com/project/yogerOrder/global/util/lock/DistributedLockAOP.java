package com.project.yogerOrder.global.util.lock;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.project.yogerOrder.global.util.spel.CustomSpringELParser;
import com.project.yogerOrder.global.util.transaction.AopForTransaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAOP {

	private static final String LOCK_PREFIX = "distributed_lock_";

	private final RedissonClient redissonClient;

	private final AopForTransaction aopForTransaction;

	@Around("@annotation(DistributedLock)")
	public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

		String key = LOCK_PREFIX + distributedLock.category() + CustomSpringELParser.getDynamicValue(
			signature.getParameterNames(),
			joinPoint.getArgs(),
			distributedLock.key()
		);

		RLock lock = redissonClient.getLock(key);

		try {
			if (lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit())) {
				return aopForTransaction.proceed(joinPoint);
			} else {
				log.warn("Failed to acquire lock for key: {}", key);
				throw new RuntimeException("Failed to acquire lock");
			}
		} catch (Throwable throwable) {
			Thread.currentThread().interrupt();
			log.error("Error occurred while processing: {}", throwable.getMessage(), throwable);
			throw new InterruptedException();
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
