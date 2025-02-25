package com.project.yogerOrder.payment.event.producer;


import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.entity.PaymentState;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.payment.event.PaymentErroredEvent;
import com.project.yogerOrder.payment.event.outbox.service.PaymentOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final PaymentOutboxService paymentOutboxService;

    public void publishEventByState(PaymentEntity paymentEntity) {
        if (paymentEntity.getState() == PaymentState.TEMPORARY_PAID) {
            // temporary paid payment is ignored
        } else if (paymentEntity.getState() == PaymentState.PAID_END) {
            publishPaymentCompletedEvent(paymentEntity);
        } else if (paymentEntity.getState() == PaymentState.CANCELED) {
            publishPaymentCanceledEvent(paymentEntity);
        } else if (paymentEntity.getState() == PaymentState.ERROR) {
            publishPaymentCanceledEvent(paymentEntity);
            publishPaymentErroredEvent(paymentEntity);
        } else {
            throw new IllegalArgumentException("Invalid payment state: " + paymentEntity.getState());
        }
    }

    private void publishPaymentCompletedEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentCompletedEvent.from(paymentEntity));
    }

    private void publishPaymentCanceledEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentCanceledEvent.from(paymentEntity));
    }

    private void publishPaymentErroredEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentErroredEvent.from(paymentEntity));
    }

}
