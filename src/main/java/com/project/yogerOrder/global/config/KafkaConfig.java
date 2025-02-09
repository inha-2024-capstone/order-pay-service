package com.project.yogerOrder.global.config;

import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.event.OrderCanceledEvent;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Configuration
    @RequiredArgsConstructor
    public static class KafkaAdminConfig {

        private final KafkaAdminConfigValue configValue;

        @ConfigurationProperties(prefix = "kafka.admin")
        public record KafkaAdminConfigValue(@NotBlank String bootstrapServers) {
        }

        @Bean
        public KafkaAdmin kafkaAdmin() {
            HashMap<String, Object> config = new HashMap<>();
            config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, configValue.bootstrapServers);

            return new KafkaAdmin(config);
        }

    }

}
