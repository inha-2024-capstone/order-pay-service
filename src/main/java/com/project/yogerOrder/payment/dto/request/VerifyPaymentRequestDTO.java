package com.project.yogerOrder.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequestDTO(@NotBlank String impUid,
                                      @NotBlank String merchantUid) {

    public static VerifyPaymentRequestDTO from(PortOnePaymentWebhookRequestDTO portOnePaymentWebhookRequestDTO) {
        return new VerifyPaymentRequestDTO(
                portOnePaymentWebhookRequestDTO.imp_uid(),
                portOnePaymentWebhookRequestDTO.merchant_uid()
        );
    }
}
