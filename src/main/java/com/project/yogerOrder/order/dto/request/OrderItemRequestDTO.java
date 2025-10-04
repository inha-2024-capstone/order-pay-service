package com.project.yogerOrder.order.dto.request;

import com.project.yogerOrder.order.entity.OrderItem;
import com.project.yogerOrder.order.event.OrderItemData;

import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(@NotNull Long productId, @NotNull Integer quantity) {

	public OrderItem toOrderItem() {
		return new OrderItem(this.productId(), this.quantity());
	}
	
	public static OrderItemRequestDTO from(OrderItemData orderItemData) {
		return new OrderItemRequestDTO(orderItemData.productId(), orderItemData.quantity());
	}
}
