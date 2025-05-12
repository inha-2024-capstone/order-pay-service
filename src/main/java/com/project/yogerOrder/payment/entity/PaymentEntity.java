package com.project.yogerOrder.payment.entity;

import com.project.yogerOrder.global.entity.BaseTimeEntity;
import com.project.yogerOrder.payment.util.stateMachine.PaymentStateChangeEvent;
import com.project.yogerOrder.payment.util.stateMachine.PaymentStaticStateMachine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_pg_payment_id", columnList = "pg_payment_id"))
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, updatable = false)
    private String pgPaymentId;

    @NotBlank
    @Column(nullable = false, unique = true, updatable = false)
    private String orderId;

    @Min(1)
    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer refundedAmount = 0;


    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState state;

    @Version
    private Long version;

    private PaymentEntity(String pgPaymentId, String orderId, Integer amount, Long userId, PaymentState state) {
        this.pgPaymentId = pgPaymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.userId = userId;
        this.state = state;
    }

    public static PaymentEntity createPaidPayment(String impUid, String orderId, Integer amount, Long userId) {
        return new PaymentEntity(impUid, orderId, amount, userId, PaymentState.PAID);
    }

    public static PaymentEntity createCanceledPayment(String impUid, String orderId, Integer amount, Long userId) {
        return new PaymentEntity(impUid, orderId, amount, userId, PaymentState.CANCELED);
    }

    public static PaymentEntity createErrorPayment(String impUid, String orderId, Integer amount, Long userId) {
        return new PaymentEntity(impUid, orderId, amount, userId, PaymentState.ERRORED);
    }

    public Boolean changeStateIfChangeable(PaymentStateChangeEvent paymentStateChangeEvent) {
        PaymentState nextState = PaymentStaticStateMachine.nextState(this.state, paymentStateChangeEvent);
        boolean isChanged = (this.state != nextState);
        this.state = nextState;

        return isChanged;
    }
}
