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

import com.project.yogerOrder.global.support.MysqlInitializer;
import com.project.yogerOrder.global.support.RedisInitializer;
import com.redis.testcontainers.RedisContainer;

@Testcontainers
@ActiveProfiles("test")
@Import({MysqlInitializer.class, RedisInitializer.class, KafkaTestConfig.class})
public abstract class UsingTestContainerTest {

    @Autowired
    private MysqlInitializer mysqlInitializer;

    @Autowired
    private RedisInitializer redisInitializer;

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

    @Container
    static final RedisContainer REDIS_CONTAINER = new RedisContainer("redis:7.0.11-alpine");

    @DynamicPropertySource
    private static void redisContainerProperties(DynamicPropertyRegistry registry) {
        String redisHost = REDIS_CONTAINER.getHost();
        Integer redisPort = REDIS_CONTAINER.getFirstMappedPort();
        registry.add("spring.redis.host", () -> redisHost);
        registry.add("spring.redis.port", () -> redisPort);
        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> redisPort);
    }


    @BeforeEach
    void delete() {
        mysqlInitializer.clear();
        redisInitializer.clear();
    }
}