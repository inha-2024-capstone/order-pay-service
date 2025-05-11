package com.project.yogerOrder.payment.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.project.yogerOrder.payment.entity.PaymentEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentErroredEvent(@NotBlank String paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                  @NotNull PaymentErroredData data, @NotNull LocalDateTime occurrenceDateTime) {

    private record PaymentErroredData(@NotNull Long userId, @NotNull String orderId, @NotNull Integer totalPrice) {
    }

    public String getOrderId() {
        return data().orderId();
    }

    public static PaymentErroredEvent from(PaymentEntity paymentEntity) {
        return new PaymentErroredEvent(
                paymentEntity.getPgPaymentId(),
                UUID.randomUUID().toString(),
                PaymentEventType.CANCELED,
                new PaymentErroredData(paymentEntity.getUserId(), paymentEntity.getOrderId(), paymentEntity.getAmount()),
                LocalDateTime.now()
        );
    }
}
