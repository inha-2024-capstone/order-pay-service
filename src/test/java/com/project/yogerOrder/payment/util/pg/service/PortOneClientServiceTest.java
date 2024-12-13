package com.project.yogerOrder.payment.util.pg.service;

import com.project.yogerOrder.payment.exception.InvalidPaymentRefundException;
import com.project.yogerOrder.payment.exception.InvalidPaymentRequestException;
import com.project.yogerOrder.payment.exception.PGServerException;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.enums.PGState;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import retrofit2.HttpException;

import java.io.IOException;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortOneClientServiceTest {

    @Mock
    IamportClient iamportClient;

    @InjectMocks
    PortOneClientService portOneClientService;

    @Test
    void getInformByIdSuccess() throws IamportResponseException, IOException {
        // given
        String paymentUid = "testPaymentId";
        String merchantUid = "testMerchantId";
        Integer amount = 1000;
        PGState state = PGState.PAID;
        Payment payment = new Payment();
        ReflectionTestUtils.setField(payment, "imp_uid", paymentUid);
        ReflectionTestUtils.setField(payment, "merchant_uid", merchantUid);
        ReflectionTestUtils.setField(payment, "amount", new BigDecimal(amount));
        ReflectionTestUtils.setField(payment, "status", state.name());

        IamportResponse<Payment> iamportResponse = new IamportResponse<>();
        ReflectionTestUtils.setField(iamportResponse, "response", payment);


        // when
        when(iamportClient.paymentByImpUid(paymentUid)).thenReturn(iamportResponse);

        PGPaymentInformResponseDTO informById = portOneClientService.getInformById(paymentUid);

        // then
        Assertions.assertThat(informById.pgPaymentId()).isEqualTo(paymentUid);
        Assertions.assertThat(informById.orderId()).isEqualTo(merchantUid);
        Assertions.assertThat(informById.amount()).isEqualTo(amount);
        Assertions.assertThat(informById.status()).isEqualTo(state);
    }

    ResponseBody emptyResponseBody = new ResponseBody() {
        @Override
        public @NotNull BufferedSource source() {
            return null;
        }
        @Override
        public @Nullable MediaType contentType() {
            return null;
        }
        @Override
        public long contentLength() {
            return 0;
        }
    };

    @Test
    void getInformByIdNotFoundFail() throws IamportResponseException, IOException {
        // given
        retrofit2.Response<Object> errorResponse = retrofit2.Response.error(HttpStatus.NOT_FOUND.value(), emptyResponseBody);
        HttpException httpException = new HttpException(errorResponse);
        IamportResponseException iamportResponseException = new IamportResponseException("errorString", httpException);

        // when
        when(iamportClient.paymentByImpUid(anyString())).thenThrow(iamportResponseException);

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.getInformById(anyString()))
                .isInstanceOf(InvalidPaymentRequestException.class);
    }

    @Test
    void getInformByIdUnauthorizedFail() throws IamportResponseException, IOException {
        // given
        retrofit2.Response<Object> errorResponse = retrofit2.Response.error(HttpStatus.UNAUTHORIZED.value(), emptyResponseBody);
        HttpException httpException = new HttpException(errorResponse);
        IamportResponseException iamportResponseException = new IamportResponseException("errorString", httpException);

        // when
        when(iamportClient.paymentByImpUid(anyString())).thenThrow(iamportResponseException);

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.getInformById(anyString()))
                .isInstanceOf(InvalidPaymentRequestException.class);
    }

    @Test
    void getInformByIdPGServerFail() throws IamportResponseException, IOException {
        // given
        retrofit2.Response<Object> errorResponse = retrofit2.Response.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), emptyResponseBody);
        HttpException httpException = new HttpException(errorResponse);
        IamportResponseException iamportResponseException = new IamportResponseException("errorString", httpException);

        // when
        when(iamportClient.paymentByImpUid(anyString())).thenThrow(iamportResponseException);

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.getInformById(anyString()))
                .isInstanceOf(PGServerException.class);
    }

    @Test
    void getInformByIdNetworkFail() throws IamportResponseException, IOException {
        // given, when
        when(iamportClient.paymentByImpUid(anyString())).thenThrow(new IOException());

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.getInformById(anyString()))
                .isInstanceOf(PGServerException.class);
    }


    @Test
    void refundUnauthorizedFail() throws IamportResponseException, IOException {
        // given
        String paymentUid = "testPaymentId";
        Integer amount = 1000;
        Integer checksum = 1000;
        PGRefundRequestDTO pgRefundRequestDTO = new PGRefundRequestDTO(paymentUid, checksum, amount);

        CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
        cancelData.setChecksum(new BigDecimal(checksum));


        retrofit2.Response<Object> errorResponse = retrofit2.Response.error(HttpStatus.UNAUTHORIZED.value(), emptyResponseBody);
        HttpException httpException = new HttpException(errorResponse);
        IamportResponseException iamportResponseException = new IamportResponseException("errorString", httpException);

        // when
        when(iamportClient.cancelPaymentByImpUid(any())).thenThrow(iamportResponseException);

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.refund(pgRefundRequestDTO))
                .isInstanceOf(InvalidPaymentRefundException.class);
    }

    @Test
    void refundPGServerFail() throws IamportResponseException, IOException {
        // given
        String paymentUid = "testPaymentId";
        Integer amount = 1000;
        Integer checksum = 1000;
        PGRefundRequestDTO pgRefundRequestDTO = new PGRefundRequestDTO(paymentUid, checksum, amount);

        CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
        cancelData.setChecksum(new BigDecimal(checksum));


        retrofit2.Response<Object> errorResponse = retrofit2.Response.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), emptyResponseBody);
        HttpException httpException = new HttpException(errorResponse);
        IamportResponseException iamportResponseException = new IamportResponseException("errorString", httpException);

        // when
        when(iamportClient.cancelPaymentByImpUid(any())).thenThrow(iamportResponseException);

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.refund(pgRefundRequestDTO))
                .isInstanceOf(PGServerException.class);
    }

    @Test
    void refundNetworkFail() throws IamportResponseException, IOException {
        // given
        String paymentUid = "testPaymentId";
        Integer amount = 1000;
        Integer checksum = 1000;
        PGRefundRequestDTO pgRefundRequestDTO = new PGRefundRequestDTO(paymentUid, checksum, amount);

        CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
        cancelData.setChecksum(new BigDecimal(checksum));

        // when
        when(iamportClient.cancelPaymentByImpUid(any())).thenThrow(new IOException());

        // then
        Assertions.assertThatThrownBy(() -> portOneClientService.refund(pgRefundRequestDTO))
                .isInstanceOf(PGServerException.class);
    }
}