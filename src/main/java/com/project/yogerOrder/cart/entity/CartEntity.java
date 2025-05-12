package com.project.yogerOrder.cart.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.project.yogerOrder.global.entity.BaseTimeEntity;

import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity extends BaseTimeEntity {

	@Id
	@Getter
	private Long userId;

	private Map<String, Integer> cartItems = new ConcurrentHashMap<>();

	@Version
	private Long version;

	private CartEntity(Long userId) {
		this.userId = userId;
	}

	public static CartEntity createEmpty(Long userId) {
		return new CartEntity(userId);
	}

	public List<Map.Entry<Long, Integer>> getItems() {
		return this.cartItems.entrySet()
			.stream().map(entry -> Map.entry(Long.valueOf(entry.getKey()), entry.getValue()))
			.toList();
	}

	public void upsertItem(Long productId, Integer quantity) {
		this.cartItems.put(String.valueOf(productId), quantity);
	}

	public void removeItems(List<Long> productIds) {
		productIds.forEach(productId -> this.cartItems.remove(String.valueOf(productId)));
	}

}
