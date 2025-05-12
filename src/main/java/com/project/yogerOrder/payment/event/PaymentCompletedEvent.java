package com.project.yogerOrder.payment.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.project.yogerOrder.payment.entity.PaymentEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCompletedEvent(@NotNull String paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                    @NotNull PaymentCompletedData data, @NotNull LocalDateTime occurrenceDateTime) {

    private record PaymentCompletedData(@NotNull Long userId, @NotNull String orderId, @NotNull Integer totalPrice) {
    }

    public String getOrderId() {
        return data().orderId();
    }

    public static PaymentCompletedEvent from(PaymentEntity paymentEntity) {
        return new PaymentCompletedEvent(
            paymentEntity.getPgPaymentId(),
            UUID.randomUUID().toString(),
            PaymentEventType.COMPLETED,
            new PaymentCompletedData(paymentEntity.getUserId(), paymentEntity.getOrderId(), paymentEntity.getAmount()),
            LocalDateTime.now()
        );
    }
}
