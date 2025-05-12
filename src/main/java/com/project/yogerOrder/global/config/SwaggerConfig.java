package com.project.yogerOrder.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    private Info apiInfo() {
        return new Info()
            .title("yoger-order")
            .description("API docs about yoger-order")
            .version("1.0.0");
    }

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")
            .scheme("bearer")
            .bearerFormat("JWT");

        return new OpenAPI()
            .components(new Components().addSecuritySchemes("Bearer Token", securityScheme))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
            .info(apiInfo());
    }
}
