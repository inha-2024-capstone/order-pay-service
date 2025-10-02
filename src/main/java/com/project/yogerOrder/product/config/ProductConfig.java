package com.project.yogerOrder.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "product")
public record ProductConfig(@NotBlank String url) {
}
