package com.project.yogerOrder.payment.util.pg.service;

import com.project.yogerOrder.global.exception.specific.NotHandledException;
import com.project.yogerOrder.payment.exception.InvalidPaymentRefundException;
import com.project.yogerOrder.payment.exception.InvalidPaymentRequestException;
import com.project.yogerOrder.payment.exception.PGServerException;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;

public interface PGClientService {

    PGPaymentInformResponseDTO getInformById(String paymentId) throws InvalidPaymentRequestException, PGServerException, NotHandledException;

    void refund(PGRefundRequestDTO pgRefundRequestDTO) throws InvalidPaymentRefundException, PGServerException, NotHandledException;
}
