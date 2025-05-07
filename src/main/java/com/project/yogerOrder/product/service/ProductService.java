package com.project.yogerOrder.product.service;

import com.project.yogerOrder.product.dto.request.UpsertProductRequestDTO;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.exception.ProductNotFoundException;

public interface ProductService {

    void upsertProduct(UpsertProductRequestDTO upsertProductRequestDTO);

    ProductResponseDTO findById(Long productId) throws ProductNotFoundException;
}
