package com.project.yogerOrder.order;

import static org.awaitility.Awaitility.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.controller.OrderController;
import com.project.yogerOrder.order.dto.request.OrderItemRequestDTO;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.order.util.duplicate.exception.OrderDuplicatedException;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.entity.PaymentState;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;
import com.project.yogerOrder.product.service.ProductService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class OrderIntegrationTest extends UsingTestContainerTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    OrderController orderController;

    @Autowired
    OrderRepository orderRepository;

    @MockBean
    ProductService productService;

    private static final Long userId = 3L;
    private static final Long paymentId = 4L;
    private static final String orderId = "tempOrderId";

    private static final Long productId1 = 1L;
    private static final Integer quantity1 = 2;
    private static final Integer orderItem1Price = 2000;
    private static final OrderItem orderItem1 = new OrderItem(productId1, quantity1);

    private static final Long productId2 = 5L;
    private static final Integer quantity2 = 3;
    private static final Integer orderItem2Price = 5000;
    private static final OrderItem orderItem2 = new OrderItem(productId2, quantity2);
    private static final List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

    private static final OrderItemRequestDTO orderItemRequestDTO1 = new OrderItemRequestDTO(productId1, quantity1);
    private static final OrderItemRequestDTO orderItemRequestDTO2 = new OrderItemRequestDTO(productId1, quantity1);
    private static final List<OrderItemRequestDTO> orderItemRequestDTOs = List.of(orderItemRequestDTO1, orderItemRequestDTO2);


    @ParameterizedTest
    @MethodSource("orderStateChangeSource")
    void orderStateChangeTest(OrderState startState, Object testEvent, String eventTopic, OrderState desiredState) {
        // given
        Integer tempPrice = 30000;
        OrderEntity initialOrder = OrderEntity.createPendingOrder(orderItems, tempPrice, userId);
        ReflectionTestUtils.setField(initialOrder, "id", orderId);
        ReflectionTestUtils.setField(initialOrder, "state", startState);
        ReflectionTestUtils.setField(initialOrder, "version", 1L);

        orderRepository.save(Objects.requireNonNull(initialOrder));

        Mockito.when(productService.findByIds(orderItems.stream().map(OrderItem::productId).toList()))
            .thenReturn(List.of(
                new ProductResponseDTO(productId1, orderItem1Price, quantity1 * 2),
                new ProductResponseDTO(productId2, orderItem2Price, quantity2 * 2)
            ));

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
                        OrderState.ERRORED
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
                        OrderState.ERRORED
                )
        );
    }

    private static ProductDeductionCompletedEvent createProductDeductionCompletedEvent() {
        return ProductDeductionCompletedEvent.of(orderId);
    }

    private static ProductDeductionFailedEvent createProductDeductionFailedEvent() {
        return ProductDeductionFailedEvent.of(orderId);
    }

    private static PaymentCompletedEvent createPaymentCompletedEvent() {
        PaymentEntity paymentEntity = new PaymentEntity(paymentId, "pgPaymentId", orderId, 30000, 0, userId,
            PaymentState.PAID, 0L);

        return PaymentCompletedEvent.from(paymentEntity);
    }

    private static PaymentCanceledEvent createPaymentFailedEvent() {
        PaymentEntity paymentEntity = new PaymentEntity(paymentId, "pgPaymentId", orderId, 30000, 30000, userId,
            PaymentState.CANCELED, 0L);

        return PaymentCanceledEvent.from(paymentEntity);
    }

    @Test
    void orderDuplicatedCheckTest() {
        // given
        String orderRequestId = "orderRequestId";
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(orderRequestId, orderItemRequestDTOs);

        Mockito.when(productService.findByIds(orderRequestDTO.orderItems().stream().map(OrderItemRequestDTO::productId).toList()))
            .thenReturn(List.of(
                new ProductResponseDTO(productId1, orderItem1Price, quantity1 * 2),
                new ProductResponseDTO(productId2, orderItem2Price, quantity2 * 2)
            ));

        //when
        orderController.orderProduct(userId, orderRequestDTO);

        // then
        Assertions.assertThrows(OrderDuplicatedException.class, () -> orderController.orderProduct(userId, orderRequestDTO));
    }

}
