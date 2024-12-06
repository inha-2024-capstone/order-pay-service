package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTOs;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void productsExpirationSuccess() {
        // given
        ArgumentCaptor<PGRefundRequestDTO> captor = ArgumentCaptor.forClass(PGRefundRequestDTO.class);

        // 첫 번 째 상품
        List<PaymentOrderDTO> paymentOrderDTOs = List.of(paymentOrderDTO);
        given(paymentRepository.findAllPaymentAndOrderByProductId(productId)).willReturn(paymentOrderDTOs);
        PartialRefundRequestDTO partialRefundRequestDTO1 = new PartialRefundRequestDTO(productId, productPrice, confirmedAmountPerQuantity);


        // 두 번 째 상품
        Integer productPrice2 = productPrice + 1000;
        Integer quantity2 = 3;
        Integer quantity3 = 2;
        Integer totalAmount2 = productPrice2 * quantity2;
        Integer totalAmount3 = productPrice2 * quantity3;

        Integer confirmedAmountPerQuantity2 = (int) (productPrice2 * 0.9);
        Integer refundAmountPerQuantity2 = productPrice2 - confirmedAmountPerQuantity2;
        Integer refundAmount2 = refundAmountPerQuantity2 * quantity2;
        Integer refundAmount3 = refundAmountPerQuantity2 * quantity3;


        Long productId2 = 2L;
        Long orderId2 = 22L;
        String impUid2 = impUid + "pay2";
        OrderEntity orderEntity2 = OrderEntity.createPendingOrder(productId2, quantity2, 2L);
        ReflectionTestUtils.setField(orderEntity2, "id", orderId2);
        PaymentEntity paymentEntity2 = PaymentEntity.createTempPaidPayment(impUid2, orderId2, totalAmount2, 2L);

        Long orderId3 = 333L;
        String impUid3 = impUid + "pay3";
        OrderEntity orderEntity3 = OrderEntity.createPendingOrder(productId2, quantity3, 2L);
        ReflectionTestUtils.setField(orderEntity3, "id", orderId3);
        PaymentEntity paymentEntity3 = PaymentEntity.createTempPaidPayment(impUid3, orderId3, totalAmount3, 2L);

        List<PaymentOrderDTO> paymentOrderDTOs2 = List.of(new PaymentOrderDTO(paymentEntity2, orderEntity2), new PaymentOrderDTO(paymentEntity3, orderEntity3));
        given(paymentRepository.findAllPaymentAndOrderByProductId(productId2)).willReturn(paymentOrderDTOs2);

        PartialRefundRequestDTO partialRefundRequestDTO2 = new PartialRefundRequestDTO(productId2, productPrice2, confirmedAmountPerQuantity2);


        // 첫 번 째 + 두 번 째 상품 종합
        PartialRefundRequestDTOs partialRefundRequestDTOs = new PartialRefundRequestDTOs(List.of(partialRefundRequestDTO1, partialRefundRequestDTO2));


        // when
        paymentService.productsExpiration(partialRefundRequestDTOs);

        // then
        verify(pgClientService, times(3)).refund(captor.capture());

        List<PGRefundRequestDTO> captoredArgs = captor.getAllValues();


        assertTrue(captoredArgs.contains(new PGRefundRequestDTO(impUid, totalAmount, refundAmount)));
        assertTrue(captoredArgs.contains(new PGRefundRequestDTO(impUid2, totalAmount2, refundAmount2)));
        assertTrue(captoredArgs.contains(new PGRefundRequestDTO(impUid3, totalAmount3, refundAmount3)));


        verify(paymentTransactionService, times(1)).refund(paymentEntity, refundAmount);
    }
}