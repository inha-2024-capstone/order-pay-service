package com.project.yogerOrder.order.event.producer;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.event.*;
import com.project.yogerOrder.order.event.outbox.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final OrderOutboxService orderOutboxService;


    public void publishEventByState(OrderEntity orderEntity, OrderState beforeState) {
        Boolean isStockOccupied = OrderState.isStockOccupied(beforeState);
        Boolean isPaymentCompleted = OrderState.isPaymentCompleted(beforeState);

        if (orderEntity.getState() == OrderState.COMPLETED) {
            publishOrderCompletedEvent(orderEntity);
        }  else if (orderEntity.getState() == OrderState.CANCELED) {
            publishOrderCanceledEvent(orderEntity, isStockOccupied, isPaymentCompleted);
        } else if (orderEntity.getState() == OrderState.ERRORED) {
            publishOrderCanceledEvent(orderEntity, isStockOccupied, isPaymentCompleted);
            publishOrderErroredEvent(orderEntity);
        } else {
            throw new IllegalArgumentException("Invalid Order State" + orderEntity.getState());
        }
    }

    public void publishOrderCreatedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.CREATED, OrderCreatedEvent.from(orderEntity));
    }

    private void publishOrderCompletedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.COMPLETED, OrderCompletedEvent.from(orderEntity));
    }

    private void publishOrderCanceledEvent(OrderEntity orderEntity, Boolean isStockOccupied, Boolean isPaymentCompleted) {
        orderOutboxService.saveOutbox(OrderEventType.CANCELED, OrderCanceledEvent.from(orderEntity, isStockOccupied, isPaymentCompleted));
    }

    private void publishOrderErroredEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.ERRORED, OrderErroredEvent.from(orderEntity));
    }

    public void publishOrderDeductionAfterCanceledEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(
                OrderEventType.DEDUCTION_AFTER_CANCELED,
                DeductionAfterOrderCanceledEvent.from(orderEntity)
        );
    }

    public void publishPaymentCompletedAfterOrderCanceledEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(
                OrderEventType.PAYMENT_COMPLETED_AFTER_CANCELED,
                PaymentCompletedAfterOrderCanceledEvent.from(orderEntity)
        );
    }
}