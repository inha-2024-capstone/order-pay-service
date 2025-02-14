package com.project.yogerOrder.order;


import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.payment.event.PaymentEventType;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;
import com.project.yogerOrder.product.event.ProductEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class OrderIntegrationTest extends UsingTestContainerTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    OrderRepository orderRepository;

    private static final Long productId = 1L;
    private static final Integer quantity = 2;
    private static final Long userId = 3L;
    private static final Integer totalPrice = 1000;
    private static final String paymentId = "4";
    private static final Long orderId = 1L;


    @ParameterizedTest
    @MethodSource("orderStateChangeSource")
    void orderStateChangeTest(OrderState startState, Object testEvent, String eventTopic, OrderState desiredState) {
        // given
        OrderEntity initialOrder = OrderEntity.createPendingOrder(productId, quantity, userId);
        ReflectionTestUtils.setField(initialOrder, "id", orderId);
        ReflectionTestUtils.setField(initialOrder, "state", startState);

        orderRepository.save(initialOrder);

        // when
        kafkaTemplate.executeInTransaction(kafkaTemplate -> {
            kafkaTemplate.send(eventTopic, testEvent);
            return null;
        });

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                // then
                .untilAsserted(() -> orderRepository.findById(orderId)
                        .ifPresentOrElse(
                                orderEntity -> Assertions.assertEquals(desiredState, orderEntity.getState()),
                                Assertions::fail
                        )
                );
    }

    private static Stream<Arguments> orderStateChangeSource() {
        ProductDeductionCompletedEvent deductionCompletedEvent = createProductDeductionCompletedEvent();
        ProductDeductionFailedEvent deductionFailedEvent = createProductDeductionFailedEvent();
        PaymentCompletedEvent paymentCompletedEvent = createPaymentCompletedEvent();
        PaymentCanceledEvent paymentFailedEvent = createPaymentFailedEvent();

        return Stream.of(
                Arguments.of(
                        OrderState.CREATED,
                        deductionCompletedEvent,
                        ProductTopic.DEDUCTION_COMPLETED,
                        OrderState.STOCK_CONFIRMED
                ),
                Arguments.of(
                        OrderState.CREATED,
                        deductionFailedEvent,
                        ProductTopic.DEDUCTION_FAILED,
                        OrderState.CANCELED
                ),
                Arguments.of(OrderState.CREATED,
                        paymentCompletedEvent,
                        PaymentTopic.COMPLETED,
                        OrderState.PAYMENT_COMPLETED
                ),
                Arguments.of(
                        OrderState.CREATED,
                        paymentFailedEvent,
                        PaymentTopic.CANCELED,
                        OrderState.CANCELED
                ),

                Arguments.of(
                        OrderState.STOCK_CONFIRMED,
                        deductionCompletedEvent,
                        ProductTopic.DEDUCTION_COMPLETED,
                        OrderState.STOCK_CONFIRMED
                ),
                Arguments.of(
                        OrderState.STOCK_CONFIRMED,
                        deductionFailedEvent,
                        ProductTopic.DEDUCTION_FAILED,
                        OrderState.CANCELED
                ),
                Arguments.of(
                        OrderState.STOCK_CONFIRMED,
                        paymentCompletedEvent,
                        PaymentTopic.COMPLETED,
                        OrderState.COMPLETED
                ),
                Arguments.of(
                        OrderState.STOCK_CONFIRMED,
                        paymentFailedEvent,
                        PaymentTopic.CANCELED,
                        OrderState.CANCELED
                ),

                Arguments.of(
                        OrderState.PAYMENT_COMPLETED,
                        deductionCompletedEvent,
                        ProductTopic.DEDUCTION_COMPLETED,
                        OrderState.COMPLETED
                ),
                Arguments.of(
                        OrderState.PAYMENT_COMPLETED,
                        deductionFailedEvent,
                        ProductTopic.DEDUCTION_FAILED,
                        OrderState.CANCELED
                ),
                Arguments.of(
                        OrderState.PAYMENT_COMPLETED,
                        paymentCompletedEvent,
                        PaymentTopic.COMPLETED,
                        OrderState.PAYMENT_COMPLETED
                ),
                Arguments.of(
                        OrderState.PAYMENT_COMPLETED,
                        paymentFailedEvent,
                        PaymentTopic.CANCELED,
                        OrderState.CANCELED
                ),

                Arguments.of(
                        OrderState.COMPLETED,
                        deductionCompletedEvent,
                        ProductTopic.DEDUCTION_COMPLETED,
                        OrderState.COMPLETED
                ),
                Arguments.of(
                        OrderState.COMPLETED,
                        deductionFailedEvent,
                        ProductTopic.DEDUCTION_FAILED,
                        OrderState.ERROR
                ),
                Arguments.of(
                        OrderState.COMPLETED,
                        paymentCompletedEvent,
                        PaymentTopic.COMPLETED,
                        OrderState.COMPLETED
                ),
                Arguments.of(
                        OrderState.COMPLETED,
                        paymentFailedEvent,
                        PaymentTopic.CANCELED,
                        OrderState.ERROR
                )
        );
    }

    private static ProductDeductionCompletedEvent createProductDeductionCompletedEvent() {
        ProductDeductionCompletedEvent.ProductDeductionCompletedData completedData =
                new ProductDeductionCompletedEvent.ProductDeductionCompletedData(orderId, quantity);

        return new ProductDeductionCompletedEvent(
                productId,
                "testEventId",
                ProductEventType.DEDUCTION_COMPLETED,
                completedData,
                LocalDateTime.now()
        );
    }

    private static ProductDeductionFailedEvent createProductDeductionFailedEvent() {
        ProductDeductionFailedEvent.ProductDeductionFailedData failedData =
                new ProductDeductionFailedEvent.ProductDeductionFailedData(orderId, quantity);

        return new ProductDeductionFailedEvent(
                productId,
                "testEventId",
                ProductEventType.DEDUCTION_FAILED,
                failedData,
                LocalDateTime.now()
        );
    }

    private static PaymentCompletedEvent createPaymentCompletedEvent() {
        PaymentCompletedEvent.PaymentCompletedData completedData =
                new PaymentCompletedEvent.PaymentCompletedData(userId, orderId, totalPrice);

        return new PaymentCompletedEvent(
                paymentId,
                "testEventId",
                PaymentEventType.COMPLETED,
                completedData,
                LocalDateTime.now()
        );
    }

    private static PaymentCanceledEvent createPaymentFailedEvent() {
        PaymentCanceledEvent.PaymentCanceledData failedData =
                new PaymentCanceledEvent.PaymentCanceledData(userId, orderId, totalPrice);

        return new PaymentCanceledEvent(
                paymentId,
                "testEventId",
                PaymentEventType.CANCELED,
                failedData,
                LocalDateTime.now()
        );
    }

}
