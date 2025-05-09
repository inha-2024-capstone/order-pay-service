package com.project.yogerOrder.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResultResponseDTO;
import com.project.yogerOrder.order.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResultResponseDTO> orderProduct(@RequestHeader("User-Id") Long userId,
                                                               @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        String orderId = orderService.orderProduct(userId, orderRequestDTO);

        return new ResponseEntity<>(new OrderResultResponseDTO(orderId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<OrderResponseDTOs> findApprovedOrdersByUserId(@RequestHeader("User-Id") Long userId) {
        return new ResponseEntity<>(orderService.findApprovedOrdersByUserId(userId), HttpStatus.OK);
    }
}

