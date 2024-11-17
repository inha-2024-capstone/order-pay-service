package com.project.yogerOrder.product.service;

import com.project.yogerOrder.product.exception.ProductInsufficientException;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.exception.ProductServerStateException;

public interface ProductService {

    ProductResponseDTO findById(Long productId) throws ProductServerStateException, ProductNotFoundException;
    
    void decreaseStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException, ProductInsufficientException;

    void increaseStock(Long productId, Integer quantity) throws ProductServerStateException, ProductNotFoundException;
}
