package com.project.yogerOrder.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional
	public void upsertProduct(UpsertProductRequestDTO upsertProductRequestDTO) {
		productRepository.save(upsertProductRequestDTO.toEntity());
	}

	@Override
	public List<ProductResponseDTO> findByIds(List<Long> productIds) throws ProductNotFoundException {
		List<ProductEntity> productEntities = productRepository.findAllById(productIds);
		if (productEntities.size() != productIds.size()) {
			throw new ProductNotFoundException();
		}

		return productEntities.stream().map(ProductResponseDTO::from).toList();
	}
}
