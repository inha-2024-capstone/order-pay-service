package com.project.yogerOrder.payment.dto.response;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import jakarta.validation.constraints.NotNull;

public record PaymentOrderDTO(@NotNull PaymentEntity payment, @NotNull OrderEntity order) {
}
