package com.project.yogerOrder.payment.util.pg.service;

import com.project.yogerOrder.global.exception.specific.NotHandledException;
import com.project.yogerOrder.payment.exception.InvalidPaymentRequestException;
import com.project.yogerOrder.payment.exception.PGServerException;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

//TODO circuit breaker
@Service
@RequiredArgsConstructor
public class PortOneClientService implements PGClientService {

    private final IamportClient iamportClient;

    @Override
    public PGPaymentInformResponseDTO getInformById(String paymentId) throws InvalidPaymentRequestException, PGServerException, NotHandledException {
        try {
            Payment payment = iamportClient.paymentByImpUid(paymentId).getResponse();

            return PGPaymentInformResponseDTO.from(payment);
        } catch (IamportResponseException e) {
            if (e.getHttpStatusCode() == HttpStatus.UNAUTHORIZED.value() || e.getHttpStatusCode() == HttpStatus.NOT_FOUND.value())
                throw new InvalidPaymentRequestException();
            else if (e.getHttpStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value())
                throw new PGServerException();
            else throw new NotHandledException(e);
        } catch (IOException e) { // network exception
            throw new PGServerException();
        }
    }


}
