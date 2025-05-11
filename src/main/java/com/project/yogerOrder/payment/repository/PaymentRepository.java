package com.project.yogerOrder.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.yogerOrder.payment.entity.PaymentEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByPgPaymentId(String pgPaymentId);

    Optional<PaymentEntity> findByOrderId(String orderId);
}
