package com.project.yogerOrder.payment.config;

import com.siot.IamportRestClient.IamportClient;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortOneConfig {

    private final PortOneV1Config config;

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(config.APIKey(), config.APISecret());
    }


    @ConfigurationProperties(prefix = "port-one.v1")
    public record PortOneV1Config(@NotBlank String APIKey, @NotBlank String APISecret) {
    }

    @ConfigurationProperties(prefix = "port-one.v2")
    public record PortOneV2Config(@DefaultValue("") String APIKey, @NotBlank String APISecret) {
    }
}
