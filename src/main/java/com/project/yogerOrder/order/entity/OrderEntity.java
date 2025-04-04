package com.project.yogerOrder.order.entity;

import java.time.LocalDateTime;

import com.project.yogerOrder.global.entity.BaseTimeEntity;
import com.project.yogerOrder.order.util.stateMachine.OrderStateChangeEvent;
import com.project.yogerOrder.order.util.stateMachine.OrderStaticStateMachine;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long productId;

    @Min(1)
    @NotNull
    private Integer quantity;

    @NotNull
    private Long buyerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderState state;

    @Version
    private Long version;

    private OrderEntity(Long productId, Integer quantity, Long buyerId, OrderState state) {
        this.productId = productId;
        this.quantity = quantity;
        this.buyerId = buyerId;
        this.state = state;
    }

    public static OrderEntity createPendingOrder(Long productId, Integer quantity, Long buyerId) {
        return new OrderEntity(productId, quantity, buyerId, OrderState.CREATED);
    }

    public Boolean isPayable(Integer validTime) {
        return ((this.state == OrderState.CREATED) || (this.state == OrderState.STOCK_CONFIRMED))
                && getCreatedTime().isAfter(LocalDateTime.now().minusMinutes(validTime));
    }

    public Boolean changeStateIfChangeable(OrderStateChangeEvent orderStateChangeEvent) {
        OrderState nextState = OrderStaticStateMachine.nextState(this.state, orderStateChangeEvent);
        boolean isChanged = (this.state != nextState);
        this.state = nextState;

        return isChanged;
    }
}
