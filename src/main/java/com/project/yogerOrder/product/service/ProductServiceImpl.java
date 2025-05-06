package com.project.yogerOrder.product.service;

import org.springframework.stereotype.Service;

import com.project.yogerOrder.product.dto.request.UpsertProductRequestDTO;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.entity.ProductEntity;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	@Override
	public void upsertProduct(UpsertProductRequestDTO upsertProductRequestDTO) {
		productRepository.save(upsertProductRequestDTO.toEntity());
	}

	@Override
	public ProductResponseDTO findById(Long productId) throws ProductNotFoundException {
		ProductEntity productEntity = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

		return ProductResponseDTO.from(productEntity);
	}
}
