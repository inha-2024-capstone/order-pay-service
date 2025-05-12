package com.project.yogerOrder.payment.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.project.yogerOrder.global.util.lock.OptimisticLockRetry;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentTransactionService paymentTransactionService;

    private final PGClientService pgClientService;

    private final OrderService orderService;


    // 웹훅인 줄 알았는데 외부 요청이었으면 -> 상관 없게 로직 작성
    // 신뢰 정보(= 사용자 조작 불가, 검증 필요): 결제 id(존재 검증), 결제 금액(원래 값과 비교), 결제 상태(paid 상태인지 검사)
    // 비신뢰 정보(= 사용자 조작 가능, 검증 필요): 주문 id(다른 주문 결제 검증 필요 X)
    @OptimisticLockRetry
    public void verifyPayment(VerifyPaymentRequestDTO verifyPaymentRequestDTO) {
        // 결제 id 존재 검증
        if (paymentRepository.existsByPgPaymentId(verifyPaymentRequestDTO.impUid())) {
            log.debug("Verifying payment {} is ignored because already exists", verifyPaymentRequestDTO.impUid());
            return;
        }

        PGPaymentInformResponseDTO pgInform = pgClientService.getInformById(verifyPaymentRequestDTO.impUid());
        OrderEntity orderEntity = orderService.findById(pgInform.orderId());
        if (!pgInform.isPaid()) { // 결제된 상태가 아니면 환불 X
            log.error("PG payment {} is not paid state", pgInform.pgPaymentId());
            PaymentEntity errorPayment = cancelPaymentByError(orderEntity, pgInform);
            paymentTransactionService.saveCanceledPayment(errorPayment);

            return;
        }

        // 결제 검증: 상태
        if (!orderService.isPayable(orderEntity)) {
            log.debug("payment {} is not payable", pgInform.pgPaymentId());
            PaymentEntity canceledPayment = cancelPaymentByValidation(orderEntity, pgInform);
            pgClientService.refund(new PGRefundRequestDTO(pgInform.pgPaymentId(), pgInform.amount()));
            paymentTransactionService.saveCanceledPayment(canceledPayment);

            return;
        }

        // 결제 검증: 금액
        if (!Objects.equals(pgInform.amount(), orderEntity.getTotalPrice())) {
            log.error("PG payment {} is invalid", pgInform.pgPaymentId());
            PaymentEntity errorPayment = cancelPaymentByError(orderEntity, pgInform);
            pgClientService.refund(new PGRefundRequestDTO(pgInform.pgPaymentId(), pgInform.amount()));
            paymentTransactionService.saveCanceledPayment(errorPayment);

            return;
        }

        paymentTransactionService.confirmPayment(
            new ConfirmPaymentRequestDTO(
                pgInform.pgPaymentId(),
                pgInform.orderId(),
                orderEntity.getBuyerId(),
                pgInform.amount()
            )
        );
    }

    private PaymentEntity cancelPaymentByError(OrderEntity orderEntity, PGPaymentInformResponseDTO pgInform) {
        return PaymentEntity.createErrorPayment(
            pgInform.pgPaymentId(),
            pgInform.orderId(),
            pgInform.amount(),
            orderEntity.getBuyerId()
        );
    }

    private PaymentEntity cancelPaymentByValidation(OrderEntity orderEntity, PGPaymentInformResponseDTO pgInform) {
        return PaymentEntity.createCanceledPayment(
            pgInform.pgPaymentId(),
            pgInform.orderId(),
            pgInform.amount(),
            orderEntity.getBuyerId()
        );
    }

    @OptimisticLockRetry
    public void orderCanceled(String orderId) {
        paymentTransactionService.orderCanceled(orderId);
    }
}
