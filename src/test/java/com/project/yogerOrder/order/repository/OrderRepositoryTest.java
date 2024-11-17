package com.project.yogerOrder.order.repository;

import com.project.yogerOrder.global.CommonRepositoryTest;
import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderRepositoryTest extends CommonRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;


    @Test
    @DisplayName("정상 저장 테스트")
    void saveDefaultTest() {
        //given
        OrderEntity pendingOrder = OrderEntity.createPendingOrder(1L, 2, 3L);
        //when
        orderRepository.save(pendingOrder);
        OrderEntity order = orderRepository.findById(pendingOrder.getId()).orElse(null);

        //then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order)
                .usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(pendingOrder);
    }

    @ParameterizedTest
    @DisplayName("주문 유효성 검사")
    @MethodSource("invalidOrderParameters")
    void validationTest(Long productId, Integer quantity, Long buyerId) {
        assertThatThrownBy(() -> {
            orderRepository.save(OrderEntity.createPendingOrder(productId, quantity, buyerId));
            orderRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    private static Stream<Arguments> invalidOrderParameters() {
        return Stream.of(
                Arguments.of(1L, 2, null),
                Arguments.of(1L, null, 3L),
                Arguments.of(null, 2, 3L),
                Arguments.of(1L, 0, 3L)
        );
    }
}