package com.project.yogerOrder.global;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;

@TestConfiguration
@EnableConfigurationProperties(KafkaTestConfig.KafkaProducerConfigValue.class)
public class KafkaTestConfig {

    private final KafkaProducerConfigValue configValue;

    public KafkaTestConfig(KafkaProducerConfigValue configValue) {
        this.configValue = configValue;
    }

    @ConfigurationProperties(prefix = "kafka.producer")
    public record KafkaProducerConfigValue(@NotBlank String bootstrapServers, @NotNull Boolean enableIdempotence,
                                           @NotNull String transactionIdPrefix) {
    }

    private HashMap<String, Object> producerConfig() {
        HashMap<String, Object> config = new HashMap<>();
        System.out.println("configValue.bootstrapServers = " + configValue.bootstrapServers);

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configValue.bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, configValue.enableIdempotence);
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, configValue.transactionIdPrefix);

        return config;
    }

    @Bean
    public KafkaTemplate<String, Object> defaultKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfig()));
    }
}
