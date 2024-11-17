package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;

    private final OrderService orderService;

    @Transactional
    public void confirmPaymentAndOrder(ConfirmPaymentRequestDTO confirmPaymentRequestDTO) {
        orderService.updateStatusToPaidById(Long.valueOf(confirmPaymentRequestDTO.pgPaymentId()));

        PaymentEntity tempPayment = PaymentEntity.createTempPaidPayment(
                confirmPaymentRequestDTO.pgPaymentId(),
                confirmPaymentRequestDTO.orderId(),
                confirmPaymentRequestDTO.amount(),
                confirmPaymentRequestDTO.buyerId()
        );

        paymentRepository.save(tempPayment);
    }
}
