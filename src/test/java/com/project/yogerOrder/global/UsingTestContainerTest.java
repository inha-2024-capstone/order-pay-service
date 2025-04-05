package com.project.yogerOrder.global;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.project.yogerOrder.global.support.DBInitializer;

@Testcontainers
@ActiveProfiles("test")
@Import({DBInitializer.class, KafkaTestConfig.class})
public abstract class UsingTestContainerTest {

    @Autowired
    private DBInitializer dbInitializer;

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL_CONTAINER = (MySQLContainer) new MySQLContainer("mysql:8.0.41")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.8.0")
    );

    @DynamicPropertySource
    private static void kafkaContainerProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = KAFKA_CONTAINER.getBootstrapServers();
        registry.add("kafka.admin.bootstrap-servers", () -> bootstrapServers);
        registry.add("kafka.producer.bootstrap-servers", () -> bootstrapServers);
        registry.add("kafka.consumer.bootstrap-servers", () -> bootstrapServers);
    }

    @BeforeEach
    void delete() {
        dbInitializer.clear();
    }
}