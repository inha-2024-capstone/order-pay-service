package com.project.yogerOrder.payment;

import static org.awaitility.Awaitility.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.event.config.OrderTopic;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.event.OrderCanceledEvent;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.entity.PaymentState;
import com.project.yogerOrder.payment.event.producer.PaymentEventProducer;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PaymentIntegrationTest extends UsingTestContainerTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    PaymentRepository paymentRepository;

    @MockitoBean
    PGClientService pgClientService;
    
    @MockitoBean
    PaymentEventProducer paymentEventProducer;
    
    @MockitoBean
    OrderService orderService;


    @Test
    void paymentStateChangeTest() {
        // given
        String impUid = "imp_123123123";
        String orderId = "123123123";
        Integer amount = 1000;
        Long userId = 1L;

        Long productId1 = 1L;
        Integer quantity1 = 2;
        OrderItem orderItem1 = new OrderItem(productId1, quantity1);
        Long productId2 = 1L;
        Integer quantity2 = 2;
        OrderItem orderItem2 = new OrderItem(productId2, quantity2);
        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);


        // payment entity 생성 후 저장
        PaymentEntity tempPaidPayment = PaymentEntity.createPaidPayment(impUid, orderId, amount, userId);
        Long paymentId = paymentRepository.save(tempPaidPayment).getId();

        // orderCanceledEvent 생성을 위한 order entity 생성 후 활용
        OrderEntity orderEntity = OrderEntity.createPendingOrder(orderItems, amount, userId);
        ReflectionTestUtils.setField(orderEntity, "id", orderId); // id 설정
        OrderCanceledEvent orderCanceledEvent = OrderCanceledEvent.from(orderEntity, false, false);

        Mockito.when(orderService.findById(orderId)).thenReturn(orderEntity);


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
