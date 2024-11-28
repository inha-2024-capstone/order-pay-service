package com.project.yogerOrder.order.util.lock.service;

import com.project.yogerOrder.global.CommonTest;
import com.project.yogerOrder.order.util.lock.entity.OrderExpireLock;
import com.project.yogerOrder.order.util.lock.repository.OrderExpireLockRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderExpireLockServiceTest extends CommonTest {

    @Autowired
    OrderExpireLockService orderExpireLockService;

    @Autowired
    OrderExpireLockRepository orderExpireLockRepository;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        orderExpireLockRepository.deleteAll();
        orderExpireLockRepository.save(new OrderExpireLock(1L, false));
    }

    @Test
    void mutualExclusionTest() throws ExecutionException, InterruptedException {
        EntityManager entityManager2 = entityManager.getEntityManagerFactory().createEntityManager();
        entityManager2.getTransaction().begin();

        Boolean isLocked = orderExpireLockService.tryLock();
        assertTrue(isLocked);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> secondLock = executorService.submit(() -> {
            EntityManager entityManager1 = entityManager.getEntityManagerFactory().createEntityManager();

            entityManager1.getTransaction().begin();
            Boolean isLocked2 = orderExpireLockService.tryLock();
            entityManager1.getTransaction().commit();
            return isLocked2;
        });
        assertFalse(secondLock.get());

        entityManager2.getTransaction().commit();
    }
}