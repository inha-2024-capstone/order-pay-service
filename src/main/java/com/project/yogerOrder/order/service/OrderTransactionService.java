package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderTransactionService {

    @Transactional
    public void updateToExpiredState(OrderEntity orderEntity) {
        orderEntity.setState(OrderState.REJECTED);
    }

    @Transactional
    public void updateToErrorState(OrderEntity orderEntity) {
        orderEntity.setState(OrderState.ERROR);
    }
}
