package com.project.yogerOrder.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequestDTO(@NotBlank String pgPaymentId,
                                       @NotBlank Long orderId,
                                       @NotNull Long buyerId,
                                       @NotNull Integer amount) {
}
