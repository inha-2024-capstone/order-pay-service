package com.project.yogerOrder.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.event.producer.PaymentEventProducer;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import com.project.yogerOrder.payment.util.stateMachine.PaymentStateChangeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class PaymentTransactionService {

    private final PaymentRepository paymentRepository;

    private final PGClientService pgClientService;

    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    void confirmPayment(ConfirmPaymentRequestDTO confirmPaymentRequestDTO) {
        PaymentEntity paymentEntity = PaymentEntity.createPaidPayment(
            confirmPaymentRequestDTO.pgPaymentId(),
            confirmPaymentRequestDTO.orderId(),
            confirmPaymentRequestDTO.amount(),
            confirmPaymentRequestDTO.buyerId()
        );
        paymentRepository.save(paymentEntity);

        paymentEventProducer.publishEventByState(paymentEntity);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    void saveCanceledPayment(PaymentEntity paymentEntity) {
        paymentRepository.save(paymentEntity);

        paymentEventProducer.publishEventByState(paymentEntity);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    void orderCanceled(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(paymentEntity -> {
            Boolean isUpdated = paymentEntity.changeStateIfChangeable(PaymentStateChangeEvent.ORDER_CANCELED);
            if (!isUpdated) {
                log.debug("payment {} is already canceled", paymentEntity.getId());
                return;
            }
            pgClientService.refund(new PGRefundRequestDTO(paymentEntity.getPgPaymentId(), paymentEntity.getAmount()));

            paymentRepository.save(paymentEntity);

            paymentEventProducer.publishEventByState(paymentEntity);
        });
    }
}
