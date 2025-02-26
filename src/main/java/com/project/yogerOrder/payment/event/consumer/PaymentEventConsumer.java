package com.project.yogerOrder.payment.event.consumer;

import com.project.yogerOrder.global.config.KafkaConfig;
import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.event.OrderCanceledEvent;
import com.project.yogerOrder.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = OrderTopic.CANCELED, groupId = KafkaConfig.PAYMENT_GROUP,
            containerFactory = KafkaConfig.KafkaConsumerConfig.ORDER_CANCELED_FACTORY)
    public void orderCanceled(OrderCanceledEvent event, Acknowledgment acknowledgment) {
        paymentService.orderCanceled(event.orderId());

        acknowledgment.acknowledge();
    }
}
