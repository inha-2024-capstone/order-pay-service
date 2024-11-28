package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTO;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.response.PaymentOrderDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.exception.InvalidPaymentRequestException;
import com.project.yogerOrder.payment.exception.PaymentAlreadyExistException;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import com.project.yogerOrder.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentTransactionService paymentTransactionService;

    private final PGClientService pgClientService;

    private final OrderService orderService;

    private final ProductService productService;


    // 웹훅인 줄 알았는데 외부 요청이었으면 -> 상관 없게 로직 작성
    // 신뢰 정보(= 사용자 조작 불가, 검증 필요): 결제 id(존재 검증), 결제 금액(원래 값과 비교), 결제 상태(paid 상태인지 검사)
    // 비신뢰 정보(= 사용자 조작 가능, 검증 필요): 주문 id(다른 주문 결제 검증 필요 X)
    //TODO error handling 필요 -> 에러를 발생시키면 api 쪽에서 응답을 보고 retry나 재시도 안하나?
    public void verifyPayment(VerifyPaymentRequestDTO verifyPaymentRequestDTO) {
        // 결제 id 존재 검증
        if (paymentRepository.existsByPgPaymentId(verifyPaymentRequestDTO.impUid())) // 내부
            throw new PaymentAlreadyExistException();

        PGPaymentInformResponseDTO pgInform = pgClientService.getInformById(verifyPaymentRequestDTO.impUid()); // 외부
        // 결제된 상태가 아니면 환불 X
        if (!pgInform.isPaid()) throw new InvalidPaymentRequestException();

        OrderEntity orderEntity = orderService.findById(Long.valueOf(pgInform.orderId())); // 내부
        Integer originalMaxPrice = productService.findById(orderEntity.getProductId()).originalMaxPrice(); // 외부

        // 내부
        // 결제 검증 = 상태, 금액
        if (pgInform.amount() != (originalMaxPrice * orderEntity.getQuantity()) || !orderService.isPayable(orderEntity)) {
            pgClientService.refund(new PGRefundRequestDTO(pgInform.pgPaymentId(), pgInform.amount())); // 외부
            throw new InvalidPaymentRequestException();
        }

        paymentTransactionService.confirmPaymentAndOrder(new ConfirmPaymentRequestDTO( // 내부
                pgInform.pgPaymentId(),
                Long.valueOf(pgInform.orderId()),
                orderEntity.getBuyerId(),
                pgInform.amount()
        ));
    }

    @Transactional
    public void productExpiration(Long productId, PartialRefundRequestDTO partialRefundRequestDTO) {
        for (PaymentOrderDTO paymentOrderDTO : paymentRepository.findAllPaymentAndOrderByProductId(productId)) {
            PaymentEntity payment = paymentOrderDTO.payment();
            OrderEntity order = paymentOrderDTO.order();
            int refundAmountPerQuantity = partialRefundRequestDTO.originalMaxPrice() - partialRefundRequestDTO.confirmedPrice();
            int refundAmount = refundAmountPerQuantity * order.getQuantity();
            int checksum = payment.getAmount();

            if (!payment.isPayCompletable()) {
                log.error("payment {} is in invalid state", payment.getId());
                payment.updateToErrorState();
                continue;
            }
            if (!Objects.equals(payment.getAmount(), partialRefundRequestDTO.originalMaxPrice() * order.getQuantity())
                    || !payment.isPartialRefundable(refundAmount)) {
                log.error("Failed to partial refund payment {} from PG", payment.getId());
                payment.updateToErrorState();
                continue;
            }

            // 외부 API 장애에 따라 db state가 영향받지 않도록, 외부 API 호출이 완료되면 entity를 update하도록 작성
            pgClientService.refund(new PGRefundRequestDTO(payment.getPgPaymentId(), checksum, refundAmount));
            paymentTransactionService.refund(payment, refundAmount);
        }
    }
}
