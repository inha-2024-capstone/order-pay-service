package com.project.yogerOrder.payment.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.project.yogerOrder.payment.entity.PaymentEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCanceledEvent(@NotBlank String paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                   @NotNull PaymentCanceledData data, @NotNull LocalDateTime occurrenceDateTime) {

    private record PaymentCanceledData(@NotNull Long userId, @NotNull String orderId, @NotNull Integer totalPrice) {
    }

    public String getOrderId() {
        return data().orderId();
    }

    public static PaymentCanceledEvent from(PaymentEntity paymentEntity) {
        return new PaymentCanceledEvent(
            paymentEntity.getPgPaymentId(),
            UUID.randomUUID().toString(),
            PaymentEventType.CANCELED,
            new PaymentCanceledData(paymentEntity.getUserId(), paymentEntity.getOrderId(), paymentEntity.getAmount()),
            LocalDateTime.now()
        );
    }
}
