package com.project.yogerOrder.order.event.consumer;

import com.project.yogerOrder.global.config.KafkaConfig;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

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
