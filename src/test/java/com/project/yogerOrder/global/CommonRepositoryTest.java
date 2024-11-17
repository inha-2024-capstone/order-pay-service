package com.project.yogerOrder.global;

import com.project.yogerOrder.global.config.JpaConfig;
import com.project.yogerOrder.global.support.DBInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;


@DataJpaTest
@Import({JpaConfig.class, DBInitializer.class})
@ActiveProfiles("test")
// dataJpaTest에 @Transactional을 포함되어 있어 각 테스트마다 flush가 일어나지 않아 트랜잭션을 분리하도록 설정이 가능하지만,
// 발생하는 예외가 wrapping되어 예상 예외를 return 받기 힘듬
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class CommonRepositoryTest {

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