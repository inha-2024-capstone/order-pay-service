package com.project.yogerOrder.order.util.lock.service;

import com.project.yogerOrder.order.util.lock.repository.OrderExpireLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExpireLockService {

    private static final Long LOCK_ID = 1L;

    private final OrderExpireLockRepository orderExpireLockRepository;

    @Transactional
    public Boolean tryLock() {
        Integer updatedCount = orderExpireLockRepository.tryLock(LOCK_ID);
        if (1 < updatedCount || updatedCount < 0) {
            log.error("Lock updated count is {}", updatedCount);
            throw new IllegalStateException("Lock updated count is " + updatedCount);
        }

        return (updatedCount == 1);
    }

    @Transactional
    public void unlock() {
        Integer updatedCount = orderExpireLockRepository.unlock(LOCK_ID);
        if (1 < updatedCount || updatedCount < 0) {
            log.error("Unlock updated count is {}", updatedCount);
            throw new IllegalStateException("Unlock updated count is " + updatedCount);
        }
    }
}
