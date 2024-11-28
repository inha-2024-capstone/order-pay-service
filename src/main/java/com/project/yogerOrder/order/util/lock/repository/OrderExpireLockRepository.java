package com.project.yogerOrder.order.util.lock.repository;

import com.project.yogerOrder.order.util.lock.entity.OrderExpireLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderExpireLockRepository extends JpaRepository<OrderExpireLock, Long> {

    @Modifying
    @Query("UPDATE OrderExpireLock o SET o.isLocked = true WHERE o.orderId = :orderId AND o.isLocked = false")
    Integer tryLock(Long orderId);

    @Modifying
    @Query("UPDATE OrderExpireLock o SET o.isLocked = false WHERE o.orderId = :orderId AND o.isLocked = true")
    Integer unlock(Long orderId);
}
