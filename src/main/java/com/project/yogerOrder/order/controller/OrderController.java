package com.project.yogerOrder.order.controller;

import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.request.OrdersCountRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResponseDTO;
import com.project.yogerOrder.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/products/{productId}")
    public ResponseEntity<OrderResponseDTO> orderProduct(@RequestHeader("User-Id") Long userId,
                                                         @PathVariable("productId") Long productId,
                                                         @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        Long orderId = orderService.orderProduct(userId, productId, orderRequestDTO);

        return new ResponseEntity<>(new OrderResponseDTO(orderId), HttpStatus.CREATED);
    }

    @GetMapping("/products/count")
    public ResponseEntity<OrderCountResponseDTOs> countOrderByProductId(@RequestBody @Valid OrdersCountRequestDTO ordersCountRequestDTO) {
        OrderCountResponseDTOs orderCountResponseDTOs = orderService.countOrdersByProductIds(ordersCountRequestDTO);

        return new ResponseEntity<>(orderCountResponseDTOs, HttpStatus.OK);
    }
}

