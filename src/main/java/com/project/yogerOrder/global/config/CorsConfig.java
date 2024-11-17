package com.project.yogerOrder.global.config;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CustomCorsConfiguration customCorsConfiguration;


    @Bean //bean으로 등록하면 withDefault로도 자동으로 적용됨
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Collections.singletonList(customCorsConfiguration.allowedOriginPatterns));
        config.setAllowedHeaders(Collections.singletonList(customCorsConfiguration.allowedHeaders));
        config.setAllowedMethods(Collections.singletonList(customCorsConfiguration.allowedMethods));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @ConfigurationProperties(prefix = "cors")
    public record CustomCorsConfiguration(@NotBlank String allowedOriginPatterns,
                                          @NotBlank String allowedHeaders,
                                          @NotBlank String allowedMethods) {}
}
