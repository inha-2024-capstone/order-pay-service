package com.project.yogerOrder.order.repository;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.yogerOrder.global.UsingTestContainerTest;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderItem;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderRepositoryTest extends UsingTestContainerTest {

    @Autowired
    private OrderRepository orderRepository;

    Long productId1 = 1L;
    Integer quantity1 = 2;
    OrderItem orderItem1 = new OrderItem(productId1, quantity1);

    Long productId2 = 5L;
    Integer quantity2 = 3;
    OrderItem orderItem2 = new OrderItem(productId2, quantity2);
    List<OrderItem> orderItems = List.of(orderItem1, orderItem2);


    @Test
    @DisplayName("정상 저장 테스트")
    void saveDefaultTest() {
        //given
        Integer totalPrice = 10000;

        OrderEntity pendingOrder = OrderEntity.createPendingOrder(orderItems, totalPrice, 3L);

        //when
        orderRepository.save(pendingOrder);
        OrderEntity order = orderRepository.findById(pendingOrder.getId()).orElse(null);

        //then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(order)
                .usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(pendingOrder);
    }
}