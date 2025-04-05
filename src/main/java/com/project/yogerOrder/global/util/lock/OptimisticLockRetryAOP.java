package com.project.yogerOrder.global.util.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class OptimisticLockRetryAOP {

	@Pointcut("@annotation(com.project.yogerOrder.global.util.lock.OptimisticLockRetry)") // 어노테이션이 붙은 메소드 포인트컷
	public void optimisticLockRetryMethods() {
	}

	@Around("optimisticLockRetryMethods() && @annotation(optimisticLockRetry)")
	public Object retry(ProceedingJoinPoint joinPoint, OptimisticLockRetry optimisticLockRetry) throws Throwable {
		int maxRetries = optimisticLockRetry.maxRetry();
		int retryCount = 0;

		while (true) {
			try {
				return joinPoint.proceed();
			} catch (OptimisticLockingFailureException e) {
				retryCount++;
				if (maxRetries <= retryCount) {
					log.error("Optimistic lock retry failed after {} attempts", retryCount, e);
					throw e; // 최대 재시도 횟수 초과 시 예외 발생
				}
			}
		}
	}
}
