package com.project.yogerOrder.payment.event;


import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.entity.PaymentState;
import com.project.yogerOrder.payment.event.outbox.service.PaymentOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final PaymentOutboxService paymentOutboxService;

    public void sendEventByState(PaymentEntity paymentEntity) {
        if (paymentEntity.getState() == PaymentState.TEMPORARY_PAID) {
            // temporary paid payment is ignored
        } else if (paymentEntity.getState() == PaymentState.PAID_END) {
            sendPaymentCompletedEvent(paymentEntity);
        } else if (paymentEntity.getState() == PaymentState.CANCELED) {
            sendPaymentCanceledEvent(paymentEntity);
        } else if (paymentEntity.getState() == PaymentState.ERROR) {
            sendPaymentCanceledEvent(paymentEntity);
            sendPaymentErroredEvent(paymentEntity);
        }

        throw new IllegalArgumentException("Invalid payment state: " + paymentEntity.getState());
    }

    private void sendPaymentCompletedEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentCompletedEvent.from(paymentEntity));
    }

    private void sendPaymentCanceledEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentCanceledEvent.from(paymentEntity));
    }

    private void sendPaymentErroredEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentErroredEvent.from(paymentEntity));
    }

}
