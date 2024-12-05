package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTO;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.response.PaymentOrderDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.enums.PGState;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.service.ProductService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private PGClientService pgClientService;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;


    String impUid = "test_imp";
    String merchantUid = "12345";
    Integer productPrice = 1000;
    Integer quantity = 3;
    Integer totalAmount = productPrice * quantity;
    Integer confirmedAmountPerQuantity = (int) (productPrice * 0.9);
    Integer refundAmountPerQuantity = productPrice - confirmedAmountPerQuantity;
    Integer refundAmount = refundAmountPerQuantity * quantity;

    Long orderId = 1L;
    Long productId = 1L;
    Long userId = 1L;



    VerifyPaymentRequestDTO requestDTO = new VerifyPaymentRequestDTO(impUid, merchantUid);
    PGPaymentInformResponseDTO pgInform = new PGPaymentInformResponseDTO(impUid, merchantUid, totalAmount, PGState.PAID);
    ProductResponseDTO productResponseDTO = new ProductResponseDTO(productId, confirmedAmountPerQuantity, productPrice);
    PaymentEntity paymentEntity = PaymentEntity.createTempPaidPayment(impUid, orderId, totalAmount, userId);
    OrderEntity orderEntity;
    PaymentOrderDTO paymentOrderDTO;


    @BeforeEach
    void beforeAll() {
        orderEntity = OrderEntity.createPendingOrder(productId, quantity, userId);
        ReflectionTestUtils.setField(orderEntity, "id", orderId);
        paymentOrderDTO = new PaymentOrderDTO(paymentEntity, orderEntity);
    }


    @Test
    void verifySuccess() {
        // given
        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(pgInform);
        given(orderService.findById(any())).willReturn(orderEntity);
        given(productService.findById(any())).willReturn(productResponseDTO);
        given(orderService.isPayable(any())).willReturn(true);
        willDoNothing().given(paymentTransactionService).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));

        // when
        paymentService.verifyPayment(requestDTO);

        // then
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    @Test
    void verifyFailByPaymentExists() {
        // given
        given(paymentRepository.existsByPgPaymentId(requestDTO.impUid())).willReturn(true);

        // when
        paymentService.verifyPayment(requestDTO);
        // then
        verify(pgClientService, times(0)).getInformById(any());
        verify(orderService, times(0)).findById(any());
        verify(productService, times(0)).findById(any());
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    @ParameterizedTest
    @MethodSource("verifyFailByStateSource")
    void verifyFailByState(PGState pgState) {
        // given
        PGPaymentInformResponseDTO currentPGInform = new PGPaymentInformResponseDTO(impUid, merchantUid, productPrice, pgState);
        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(currentPGInform);

        // when
        paymentService.verifyPayment(requestDTO);

        // then
        verify(orderService, times(0)).findById(any());
        verify(productService, times(0)).findById(any());
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    private static Stream<Arguments> verifyFailByStateSource() {
        return Arrays.stream(PGState.values())
                .filter(state -> !state.isPaid())
                .map(Arguments::of);
    }


    @Test
    void verifyFailByAmount() {
        // given
        ArgumentCaptor<PGRefundRequestDTO> captor = ArgumentCaptor.forClass(PGRefundRequestDTO.class);

        PGPaymentInformResponseDTO invalidPGInform = new PGPaymentInformResponseDTO(
                impUid, merchantUid, pgInform.amount() - 1000, PGState.PAID
        );

        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(invalidPGInform);
        given(orderService.findById(any())).willReturn(orderEntity);
        given(productService.findById(any())).willReturn(productResponseDTO);
        lenient().when(orderService.isPayable(any())).thenReturn(true);
        willDoNothing().given(pgClientService).refund(any(PGRefundRequestDTO.class));

        // when
        paymentService.verifyPayment(requestDTO);

        // when, then
        verify(pgClientService, times(1)).refund(captor.capture());
        Assertions.assertThat(captor.getValue().paymentId()).isEqualTo(pgInform.pgPaymentId());
        Assertions.assertThat(captor.getValue().checksum()).isEqualTo(invalidPGInform.amount());
        Assertions.assertThat(captor.getValue().refundAmount()).isEqualTo(invalidPGInform.amount());
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    @Test
    void productExpirationSuccess() {
        // given
        List<PaymentOrderDTO> paymentOrderDTOs = List.of(paymentOrderDTO);
        given(paymentRepository.findAllPaymentAndOrderByProductId(productId)).willReturn(paymentOrderDTOs);
        willDoNothing().given(pgClientService).refund(any());
        willDoNothing().given(paymentTransactionService).refund(any(), anyInt());
        PartialRefundRequestDTO partialRefundRequestDTO = new PartialRefundRequestDTO(productPrice, confirmedAmountPerQuantity);

        // when
        paymentService.productExpiration(productId, partialRefundRequestDTO);

        // then
        verify(pgClientService, times(paymentOrderDTOs.size())).refund(new PGRefundRequestDTO(impUid, totalAmount, refundAmount));
        verify(paymentTransactionService, times(paymentOrderDTOs.size())).refund(paymentEntity, refundAmount);
    }
}