package com.project.yogerOrder.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PortOnePaymentWebhookRequestDTO(@NotBlank String imp_uid,
                                              @NotBlank String merchant_uid,
                                              @NotBlank String status) {
}
