package com.project.yogerOrder.order.controller;

import com.project.yogerOrder.global.config.KafkaConfig;
import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.request.OrdersCountRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResultResponseDTO;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/products/{productId}")
    public ResponseEntity<OrderResultResponseDTO> orderProduct(@RequestHeader("User-Id") Long userId,
                                                               @PathVariable("productId") Long productId,
                                                               @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        Long orderId = orderService.orderProduct(userId, productId, orderRequestDTO);

        return new ResponseEntity<>(new OrderResultResponseDTO(orderId), HttpStatus.CREATED);
    }

    @PostMapping("/products/count")
    public ResponseEntity<OrderCountResponseDTOs> countOrderByProductId(@RequestBody @Valid OrdersCountRequestDTO ordersCountRequestDTO) {
        return new ResponseEntity<>(orderService.countOrdersByProductIds(ordersCountRequestDTO), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<OrderResponseDTOs> findApprovedOrdersByUserId(@RequestHeader("User-Id") Long userId) {
        return new ResponseEntity<>(orderService.findApprovedOrdersByUserId(userId), HttpStatus.OK);
    }

    @KafkaListener(topics = ProductTopic.DEDUCTION_COMPLETED, groupId = KafkaConfig.ORDER_GROUP,
            containerFactory = KafkaConfig.KafkaConsumerConfig.DEDUCTION_COMPLETED_FACTORY)
    public void productDeductionCompleted(ProductDeductionCompletedEvent event, Acknowledgment acknowledgment) {
        orderService.updateByDeductionSuccess(event.data().orderId());

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = ProductTopic.DEDUCTION_FAILED, groupId = KafkaConfig.ORDER_GROUP,
            containerFactory = KafkaConfig.KafkaConsumerConfig.DEDUCTION_FAILED_FACTORY)
    public void productDeductionFailed(ProductDeductionFailedEvent event, Acknowledgment acknowledgment) {
        orderService.updateByDeductionFail(event.data().orderId());

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = PaymentTopic.COMPLETED, groupId = KafkaConfig.ORDER_GROUP,
            containerFactory = KafkaConfig.KafkaConsumerConfig.PAYMENT_COMPLETED_FACTORY)
    public void paymentCompleted(PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        orderService.updateByPaymentCompleted(event.data().orderId());

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = PaymentTopic.CANCELED, groupId = KafkaConfig.ORDER_GROUP,
            containerFactory = KafkaConfig.KafkaConsumerConfig.PAYMENT_CANCELED_FACTORY)
    public void paymentCanceled(PaymentCanceledEvent event, Acknowledgment acknowledgment) {
        orderService.updateByPaymentCanceled(event.data().orderId());

        acknowledgment.acknowledge();
    }
}

