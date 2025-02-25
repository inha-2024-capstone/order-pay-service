package com.project.yogerOrder.global;

import com.project.yogerOrder.global.support.DBInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
@Import({DBInitializer.class, KafkaTestConfig.class})
public abstract class UsingTestContainerTest {
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1234";

    @Autowired
    private DBInitializer dbInitializer;

    @Container
    static final MySQLContainer MYSQL_CONTAINER = (MySQLContainer) new MySQLContainer("mysql:latest")
            .withDatabaseName("test")
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD)
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:5.4.3")
    );


    @DynamicPropertySource
    private static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }

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