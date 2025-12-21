package com.project.yogerOrder.order.event.consumer;

 import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.project.yogerOrder.global.config.KafkaConfig;
 import com.project.yogerOrder.order.dto.request.ConfirmReservationsRequestDTO;
 import com.project.yogerOrder.order.event.OrderCompletedEvent;
 import com.project.yogerOrder.order.event.config.OrderTopic;
 import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.event.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.product.event.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = ProductTopic.DEDUCTION_COMPLETED, groupId = KafkaConfig.ORDER_GROUP,
        containerFactory = KafkaConfig.KafkaConsumerConfig.DEDUCTION_COMPLETED_FACTORY)
    public void productDeductionCompleted(ProductDeductionCompletedEvent event, Acknowledgment acknowledgment) {
        orderService.updateByDeductionSuccess(event.getOrderId());

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = ProductTopic.DEDUCTION_FAILED, groupId = KafkaConfig.ORDER_GROUP,
        containerFactory = KafkaConfig.KafkaConsumerConfig.DEDUCTION_FAILED_FACTORY)
    public void productDeductionFailed(ProductDeductionFailedEvent event, Acknowledgment acknowledgment) {
        orderService.updateByDeductionFail(event.getOrderId());

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = PaymentTopic.COMPLETED, groupId = KafkaConfig.ORDER_GROUP,
        containerFactory = KafkaConfig.KafkaConsumerConfig.PAYMENT_COMPLETED_FACTORY)
    public void paymentCompleted(PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        orderService.updateByPaymentCompleted(event.getOrderId());

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = PaymentTopic.CANCELED, groupId = KafkaConfig.ORDER_GROUP,
        containerFactory = KafkaConfig.KafkaConsumerConfig.PAYMENT_CANCELED_FACTORY)
    public void paymentCanceled(PaymentCanceledEvent event, Acknowledgment acknowledgment) {
        orderService.updateByPaymentCanceled(event.getOrderId());

        acknowledgment.acknowledge();
    }
    
    @KafkaListener(topics = OrderTopic.COMPLETED, groupId = KafkaConfig.ORDER_GROUP,
        containerFactory = KafkaConfig.KafkaConsumerConfig.ORDER_COMPLETED_FACTORY)
    @Transactional(transactionManager = "kafkaTransactionManager")
    public void orderCompleted(OrderCompletedEvent event, Acknowledgment acknowledgment) {
        orderService.confirmReservations(
            new ConfirmReservationsRequestDTO(event.orderId(), event.getUserId(), event.getOrderItems())
        );
        
        acknowledgment.acknowledge();
    }
}
