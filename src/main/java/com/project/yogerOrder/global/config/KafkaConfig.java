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
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    public static final String ORDER_GROUP = "order-group";
    public static final String PAYMENT_GROUP = "payment-group";

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

    @Configuration
    @RequiredArgsConstructor
    public static class KafkaConsumerConfig {

        private final KafkaConsumerConfigValue configValue;

        @ConfigurationProperties(prefix = "kafka.consumer")
        public record KafkaConsumerConfigValue(@NotBlank String bootstrapServers, @NotBlank String isolationLevel,
                                               @NotBlank String autoOffsetReset, @NotNull Boolean enableAutoCommit) {
        }

        private HashMap<String, Object> consumerConfig() {
            HashMap<String, Object> config = new HashMap<>();
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configValue.bootstrapServers);
            config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configValue.autoOffsetReset);
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configValue.enableAutoCommit);
            config.put(ConsumerConfig.GROUP_ID_CONFIG, ORDER_GROUP);
            config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

            return config;
        }

        public static final String ORDER_CANCELED_FACTORY = "orderCanceledFactory";

        public static final String DEDUCTION_COMPLETED_FACTORY = "productDeductionCompletedFactory";
        public static final String DEDUCTION_FAILED_FACTORY = "productDeductionFailedFactory";

        public static final String PAYMENT_COMPLETED_FACTORY = "paymentCompletedFactory";
        public static final String PAYMENT_CANCELED_FACTORY = "paymentCanceledFactory";


        @Bean(ORDER_CANCELED_FACTORY)
        public ConcurrentKafkaListenerContainerFactory<String, OrderCanceledEvent> orderpaymentCanceledEventConcurrentKafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, OrderCanceledEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

            DefaultKafkaConsumerFactory<String, OrderCanceledEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                    consumerConfig(),
                    new StringDeserializer(),
                    new JsonDeserializer<>(OrderCanceledEvent.class, false)
            );

            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }

        @Bean(DEDUCTION_COMPLETED_FACTORY)
        public ConcurrentKafkaListenerContainerFactory<String, ProductDeductionCompletedEvent> productDeductionCompletedEventConcurrentKafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, ProductDeductionCompletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

            DefaultKafkaConsumerFactory<String, ProductDeductionCompletedEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                    consumerConfig(),
                    new StringDeserializer(),
                    new JsonDeserializer<>(ProductDeductionCompletedEvent.class, false)
            );

            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }

        @Bean(DEDUCTION_FAILED_FACTORY)
        public ConcurrentKafkaListenerContainerFactory<String, ProductDeductionFailedEvent> productDeductionFailedEventConcurrentKafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, ProductDeductionFailedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

            DefaultKafkaConsumerFactory<String, ProductDeductionFailedEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                    consumerConfig(),
                    new StringDeserializer(),
                    new JsonDeserializer<>(ProductDeductionFailedEvent.class, false)
            );

            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }


        @Bean(PAYMENT_COMPLETED_FACTORY)
        public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedEventConcurrentKafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

            DefaultKafkaConsumerFactory<String, PaymentCompletedEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                    consumerConfig(),
                    new StringDeserializer(),
                    new JsonDeserializer<>(PaymentCompletedEvent.class, false)
            );

            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }

        @Bean(PAYMENT_CANCELED_FACTORY)
        public ConcurrentKafkaListenerContainerFactory<String, PaymentCanceledEvent> paymentCanceledEventConcurrentKafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, PaymentCanceledEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

            DefaultKafkaConsumerFactory<String, PaymentCanceledEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                    consumerConfig(),
                    new StringDeserializer(),
                    new JsonDeserializer<>(PaymentCanceledEvent.class, false)
            );

            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }
    }

    @Bean
    public KafkaAdmin.NewTopics orderTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(OrderTopic.CREATED).build(),
                TopicBuilder.name(OrderTopic.COMPLETED).build(),
                TopicBuilder.name(OrderTopic.CANCELED).build(),
                TopicBuilder.name(OrderTopic.ERRORED).build()
        );
    }

    @Bean
    public KafkaAdmin.NewTopics paymentTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(PaymentTopic.COMPLETED).build(),
                TopicBuilder.name(PaymentTopic.CANCELED).build(),
                TopicBuilder.name(PaymentTopic.ERRORED).build()
        );
    }

    @Bean
    public KafkaAdmin.NewTopics productTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(ProductTopic.DEDUCTION_COMPLETED).build(),
                TopicBuilder.name(ProductTopic.DEDUCTION_FAILED).build()
        );
    }

}
