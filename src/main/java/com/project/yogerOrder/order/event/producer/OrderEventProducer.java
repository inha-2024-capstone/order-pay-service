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


    public void publishEventByState(OrderEntity orderEntity) {
        if (orderEntity.getState() == OrderState.CREATED) {
            publishOrderCreatedEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.STOCK_CONFIRMED) {
            // stock confirmed event ignored
        } else if (orderEntity.getState() == OrderState.PAYMENT_COMPLETED) {
            // payment completed event ignored
        } else if (orderEntity.getState() == OrderState.COMPLETED) {
            publishOrderCompletedEvent(orderEntity);
        }  else if (orderEntity.getState() == OrderState.CANCELED) {
            publishOrderCanceledEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.ERROR) {
            publishOrderCanceledEvent(orderEntity);
            publishOrderErroredEvent(orderEntity);
        }

        throw new IllegalArgumentException("Invalid Order State" + orderEntity.getState());
    }

    private void publishOrderCreatedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.CREATED, OrderCreatedEvent.from(orderEntity));
    }

    private void publishOrderCompletedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.COMPLETED, OrderCompletedEvent.from(orderEntity));
    }

    private void publishOrderCanceledEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.CANCELED, OrderCanceledEvent.from(orderEntity));
    }

    private void publishOrderErroredEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(OrderEventType.ERRORED, OrderErroredEvent.from(orderEntity));
    }
}