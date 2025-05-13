package com.project.yogerOrder.payment.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.project.yogerOrder.payment.entity.PaymentEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCanceledEvent(@NotBlank Long paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                   @NotNull PaymentCanceledData data, @NotNull LocalDateTime occurrenceDateTime) {

    private record PaymentCanceledData(
        @NotNull String pgPaymentId,
        @NotNull Long userId,
        @NotNull String orderId,
        @NotNull Integer amount) {
    }

    public String getOrderId() {
        return data().orderId();
    }

    public String getPGPaymentId() {
        return data().pgPaymentId();
    }

    public Integer getAmount() {
        return data().amount();
    }

    public static PaymentCanceledEvent from(PaymentEntity paymentEntity) {
        return new PaymentCanceledEvent(
            paymentEntity.getId(),
            UUID.randomUUID().toString(),
            PaymentEventType.CANCELED,
            new PaymentCanceledData(
                paymentEntity.getPgPaymentId(),
                paymentEntity.getUserId(),
                paymentEntity.getOrderId(),
                paymentEntity.getAmount()
            ),
            LocalDateTime.now()
        );
    }
}
