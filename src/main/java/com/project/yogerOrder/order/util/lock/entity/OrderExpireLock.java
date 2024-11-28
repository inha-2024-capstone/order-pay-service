package com.project.yogerOrder.order.util.lock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OrderExpireLock {

    @Id
    private Long orderId;

    @Column(nullable = false)
    private Boolean isLocked;
}
