package com.project.yogerOrder.product.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "product")
public record ProductConfig(@NotBlank String url) {
}
