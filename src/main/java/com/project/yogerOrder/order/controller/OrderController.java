package com.project.yogerOrder.order.controller;

import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTO;
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
    public ResponseEntity<OrderResponseDTO> orderProduct(@RequestHeader("userId") Long userId,
                                                         @PathVariable("productId") Long productId,
                                                         @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        Long orderId = orderService.orderProduct(userId, productId, orderRequestDTO);

        return new ResponseEntity<>(new OrderResponseDTO(orderId), HttpStatus.CREATED);
    }

    @PostMapping("/products/{productId}/count")
    public ResponseEntity<OrderCountResponseDTO> countOrderByProductId(@PathVariable("productId") Long productId) {
        Integer count = orderService.countOrdersByProductId(productId);

        return new ResponseEntity<>(new OrderCountResponseDTO(count),HttpStatus.OK);
    }
}

