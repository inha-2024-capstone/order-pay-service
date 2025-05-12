package com.project.yogerOrder.order.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.project.yogerOrder.global.entity.MongoDBBaseTimeEntity;
import com.project.yogerOrder.order.util.stateMachine.OrderStateChangeEvent;
import com.project.yogerOrder.order.util.stateMachine.OrderStaticStateMachine;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "order")
public class OrderEntity extends MongoDBBaseTimeEntity<String> {

    @Id
    private String id;

    @NotNull
    private Long buyerId;

    @NotEmpty
    private List<OrderItem> orderItems;

    @NotNull
    @Min(0)
    private Integer totalPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderState state;

    @Version
    private Long version;


    private OrderEntity(List<OrderItem> orderItems, Long buyerId, Integer totalPrice, OrderState state) {
        this.orderItems = orderItems;
        this.buyerId = buyerId;
        this.totalPrice = totalPrice;
        this.state = state;
    }


    public static OrderEntity createPendingOrder(List<OrderItem> orderItems, Integer totalPrice, Long buyerId) {
        return new OrderEntity(orderItems, buyerId, totalPrice, OrderState.CREATED);
    }


    public Boolean isPayable(Integer validTime) {
        return this.state.isPayable() && getCreatedTime().isAfter(LocalDateTime.now().minusMinutes(validTime));
    }

    public Boolean changeStateIfChangeable(OrderStateChangeEvent orderStateChangeEvent) {
        OrderState nextState = OrderStaticStateMachine.nextState(this.state, orderStateChangeEvent);
        boolean isChanged = (this.state != nextState);
        this.state = nextState;

        return isChanged;
    }

}
