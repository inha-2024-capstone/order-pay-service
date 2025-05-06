package com.project.yogerOrder.product.entity;

import com.project.yogerOrder.global.entity.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductEntity extends BaseTimeEntity {

	@Id
	private Long id;

	@NotBlank
	private String name;

	@NotNull
	@Min(0)
	private Integer stock;

	@NotNull
	@Min(0)
	private Integer price;

}
