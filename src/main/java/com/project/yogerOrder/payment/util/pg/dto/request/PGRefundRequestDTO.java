package com.project.yogerOrder.payment.util.pg.dto.request;

import com.project.yogerOrder.payment.exception.InvalidPaymentRefundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PGRefundRequestDTO(@NotBlank String paymentId,
                                 @NotNull @Min(1) Integer checksum,
                                 @NotNull @Min(1) Integer refundAmount) {

    public PGRefundRequestDTO {
        // 현재 금액보다 취소 금액이 더 클 경우
        if (checksum < refundAmount) throw new InvalidPaymentRefundException();
    }

    public PGRefundRequestDTO(String paymentId, Integer checksum) { // 전부 환불할 경우
        this(paymentId, checksum, checksum);
    }
}
