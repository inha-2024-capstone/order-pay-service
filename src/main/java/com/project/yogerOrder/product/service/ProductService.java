package com.project.yogerOrder.product.service;

import java.util.List;

import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.product.dto.request.UpsertProductRequestDTO;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.ProductInsufficientException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.ProductServerStateException;

public interface ProductService {
    
    void reserveStocks(String orderId, List<OrderItem> orderItems) throws ProductServerStateException, ProductNotFoundException, ProductInsufficientException;

    void upsertProduct(UpsertProductRequestDTO upsertProductRequestDTO);

    List<ProductResponseDTO> findByIds(List<Long> productIds) throws ProductNotFoundException;
}
