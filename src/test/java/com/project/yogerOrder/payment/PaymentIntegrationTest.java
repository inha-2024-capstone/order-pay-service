package com.project.yogerOrder.payment;


import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.event.OrderCanceledEvent;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.entity.PaymentState;
import com.project.yogerOrder.payment.event.PaymentEventProducer;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PaymentIntegrationTest extends UsingTestContainerTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    PaymentRepository paymentRepository;

    @MockBean
    PGClientService pgClientService;

    @MockBean
    PaymentEventProducer paymentEventProducer;


    @Test
    void paymentStateChangeTest() {
        // given
        Long paymentId = 1L;
        String impUid = "imp_123123123";
        Long orderId = 123123L;
        Integer amount = 1000;
        Long userId = 1L;

        Long productId = 1L;
        Integer quantity = 2;


        PaymentEntity tempPaidPayment = PaymentEntity.createTempPaidPayment(impUid, orderId, amount, userId);
        ReflectionTestUtils.setField(tempPaidPayment, "id", paymentId);

        paymentRepository.save(tempPaidPayment);

        OrderEntity orderEntity = OrderEntity.createPendingOrder(productId, quantity, userId);
        ReflectionTestUtils.setField(orderEntity, "id", orderId);
        OrderCanceledEvent orderCanceledEvent = OrderCanceledEvent.from(orderEntity);


        // when
        kafkaTemplate.executeInTransaction(kafkaTemplate -> {
            kafkaTemplate.send(OrderTopic.CANCELED, orderCanceledEvent);
            return null;
        });

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(30))
                // then
                .untilAsserted(() -> paymentRepository.findById(paymentId)
                        .ifPresentOrElse(
                                paymentEntity -> Assertions.assertEquals(PaymentState.CANCELED, paymentEntity.getState()),
                                Assertions::fail
                        )
                );
    }

}
