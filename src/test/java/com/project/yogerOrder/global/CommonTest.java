package com.project.yogerOrder.global;

import com.project.yogerOrder.global.support.DBInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(DBInitializer.class)
@ActiveProfiles("test")
public abstract class CommonTest {

    private static final String ROOT = "root";

    private static final String PASSWORD = "1234";


    @Autowired
    private DBInitializer dbInitializer;

    @Container
    protected static MySQLContainer mySQLContainer;

    @DynamicPropertySource
    private static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    static {
        mySQLContainer = (MySQLContainer) new MySQLContainer("mysql:latest")
                .withDatabaseName("test")
                .withUsername(ROOT)
                .withPassword(PASSWORD)
                .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

        mySQLContainer.start();
    }

    @BeforeEach
    void delete() {
        dbInitializer.clear();
    }
}