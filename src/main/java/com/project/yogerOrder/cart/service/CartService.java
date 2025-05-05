package com.project.yogerOrder.cart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yogerOrder.cart.dto.request.UpsertProductToCartRequestDTO;
import com.project.yogerOrder.cart.dto.request.DeleteProductFromCartRequestDTO;
import com.project.yogerOrder.cart.entity.CartEntity;
import com.project.yogerOrder.cart.repository.CartRepository;
import com.project.yogerOrder.global.config.MongoDBConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;


	@Transactional(transactionManager = MongoDBConfig.MONGO_TRANSACTION_MANAGER)
	public void upsertItem(Long userId, UpsertProductToCartRequestDTO requestDTO) {
		CartEntity cart = getCart(userId);
		cart.upsertItem(requestDTO.productId(), requestDTO.quantity());

		cartRepository.save(cart);
	}

	public CartEntity getCart(Long userId) {
		Optional<CartEntity> optionalCart = cartRepository.findById(userId);
		if (optionalCart.isPresent()) {
			 return optionalCart.get();
		}

		CartEntity cartEntity = CartEntity.createEmpty(userId);
		cartRepository.save(cartEntity);

		return cartEntity;
	}

	@Transactional(transactionManager = MongoDBConfig.MONGO_TRANSACTION_MANAGER)
	public void deleteItems(Long userId, DeleteProductFromCartRequestDTO requestDTO) {
		CartEntity cart = getCart(userId);
		cart.removeItems(requestDTO.productIds());

		cartRepository.save(cart);
	}

}
