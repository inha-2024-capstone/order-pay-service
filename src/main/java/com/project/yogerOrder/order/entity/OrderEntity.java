package com.project.yogerOrder.order.entity;

import com.project.yogerOrder.global.entity.BaseTimeEntity;
import com.project.yogerOrder.order.exception.IllegalOrderStateUpdateException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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


    private OrderEntity(Long productId, Integer quantity, Long buyerId, OrderState state) {
        this.productId = productId;
        this.quantity = quantity;
        this.buyerId = buyerId;
        this.state = state;
    }

    public static OrderEntity createPendingOrder(Long productId, Integer quantity, Long buyerId) {
        return new OrderEntity(productId, quantity, buyerId, OrderState.PENDING);
    }

    public Boolean isPayable(Integer validTime) {
        return this.state == OrderState.PENDING
                && getCreatedTime().isAfter(LocalDateTime.now().minusMinutes(validTime));
    }

    public void approve() {
        if (this.state != OrderState.PENDING) {
            throw new IllegalOrderStateUpdateException();
        }
        this.state = OrderState.APPROVED;
    }

    public void reject() {
        if (this.state != OrderState.PENDING) {
            throw new IllegalOrderStateUpdateException();
        }
        this.state = OrderState.REJECTED;
    }

    public void error() {
        if (this.state != OrderState.PENDING) {
            throw new IllegalOrderStateUpdateException();
        }
        this.state = OrderState.ERROR;
    }
}
