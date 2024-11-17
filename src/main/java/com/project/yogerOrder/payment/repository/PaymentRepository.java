package com.project.yogerOrder.payment.repository;

import com.project.yogerOrder.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByPgPaymentId(String pgPaymentId);
}
