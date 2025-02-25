package com.project.yogerOrder.payment;


import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.event.OrderCanceledEvent;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.entity.PaymentState;
import com.project.yogerOrder.payment.event.producer.PaymentEventProducer;
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


        // payment entity 생성 후 저장
        PaymentEntity tempPaidPayment = PaymentEntity.createPaidPayment(impUid, orderId, amount, userId);
        ReflectionTestUtils.setField(tempPaidPayment, "id", paymentId); // id 설정
        paymentRepository.save(tempPaidPayment);

        // orderCanceledEvent 생성을 위한 order entity 생성 후 활용
        OrderEntity orderEntity = OrderEntity.createPendingOrder(productId, quantity, userId);
        ReflectionTestUtils.setField(orderEntity, "id", orderId); // id 설정
        OrderCanceledEvent orderCanceledEvent = OrderCanceledEvent.from(orderEntity, false, false);


        // when
        kafkaTemplate.executeInTransaction(kafkaTemplate -> {
            kafkaTemplate.send(OrderTopic.CANCELED, orderCanceledEvent); // orderCanceledEvent 발행
            return null;
        });

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(30))
                // then payment state가 canceled로 변경되었는지 확인
                .untilAsserted(() -> paymentRepository.findById(paymentId)
                        .ifPresentOrElse(
                                paymentEntity -> Assertions.assertEquals(PaymentState.CANCELED, paymentEntity.getState()),
                                Assertions::fail
                        )
                );
    }

}
