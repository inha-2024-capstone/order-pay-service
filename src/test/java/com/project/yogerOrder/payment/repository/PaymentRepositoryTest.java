package com.project.yogerOrder.payment.repository;

import com.project.yogerOrder.global.CommonRepositoryTest;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.payment.dto.response.PaymentOrderDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class PaymentRepositoryTest extends CommonRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Test
    void findAllPaymentAndOrderByProductId() {
        // given
        String pgId = "test_pg_id";
        Integer productPrice = 1000;
        Integer quantity = 3;
        Integer totalAmount = productPrice * quantity;

        Long productId = 1L;
        Long userId = 1L;

        OrderEntity orderEntity = OrderEntity.createPendingOrder(productId, quantity, userId);
        Long orderId = orderRepository.save(orderEntity).getId();

        PaymentEntity paymentEntity = PaymentEntity.createPaidPayment(pgId, orderId, totalAmount, userId);
        paymentRepository.save(paymentEntity);

        // when
        List<PaymentOrderDTO> result = paymentRepository.findAllPaymentAndOrderByProductId(productId);

        // then
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO(paymentEntity, orderEntity);
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(List.of(paymentOrderDTO));
    }
}