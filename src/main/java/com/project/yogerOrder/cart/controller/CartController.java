package com.project.yogerOrder.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.yogerOrder.cart.dto.request.DeleteProductFromCartRequestDTO;
import com.project.yogerOrder.cart.dto.request.UpsertProductToCartRequestDTO;
import com.project.yogerOrder.cart.dto.response.CartResponseDTO;
import com.project.yogerOrder.cart.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

	private final CartService cartService;


	@PostMapping
	public ResponseEntity<Void> upsertItem(@RequestHeader("User-Id") Long userId,
		@RequestBody @Valid UpsertProductToCartRequestDTO requestDTO) {
		cartService.upsertItem(userId, requestDTO);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long userId) {
		return new ResponseEntity<>(CartResponseDTO.from(cartService.getCart(userId)), HttpStatus.OK);
	}

	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteItems(@PathVariable Long userId,
		@RequestBody @Valid DeleteProductFromCartRequestDTO requestDTO) {
		cartService.deleteItems(userId, requestDTO);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
