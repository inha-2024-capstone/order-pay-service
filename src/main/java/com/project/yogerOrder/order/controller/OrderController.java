package com.project.yogerOrder.order.controller;

import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.request.OrdersCountRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResultResponseDTO;
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
    public ResponseEntity<OrderResultResponseDTO> orderProduct(@RequestHeader("User-Id") Long userId,
                                                               @PathVariable("productId") Long productId,
                                                               @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        Long orderId = orderService.orderProduct(userId, productId, orderRequestDTO);

        return new ResponseEntity<>(new OrderResultResponseDTO(orderId), HttpStatus.CREATED);
    }

    @PostMapping("/products/count")
    public ResponseEntity<OrderCountResponseDTOs> countOrderByProductId(@RequestBody @Valid OrdersCountRequestDTO ordersCountRequestDTO) {
        return new ResponseEntity<>(orderService.countOrdersByProductIds(ordersCountRequestDTO), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<OrderResponseDTOs> findApprovedOrdersByUserId(@RequestHeader("User-Id") Long userId) {
        return new ResponseEntity<>(orderService.findApprovedOrdersByUserId(userId), HttpStatus.OK);
    }
}

