package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.dto.request.OrderItemRequestDTO;
import com.project.yogerOrder.order.entity.OrderItem;

import jakarta.validation.constraints.NotNull;

public record OrderItemData(@NotNull Long productId, @NotNull Integer quantity) {

	public static OrderItemData from(OrderItem orderItem) {
		return new OrderItemData(orderItem.productId(), orderItem.quantity());
	}


	OrderItemRequestDTO toDTO() {
		return new OrderItemRequestDTO(this.productId, this.quantity);
	}
}
