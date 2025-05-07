package com.project.yogerOrder.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;

import com.project.yogerOrder.global.util.db.ExcludeFromJpaRepository;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaAuditing // 테스트 시에 관련 bean을 로드하지 않기 때문에, 에러 발생 가능성이 있기에 메인에 적용하지 않음
@EnableJpaRepositories(
    basePackages = "com.project.yogerOrder",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = ExcludeFromJpaRepository.class
    )
)
public class JpaConfig {

    @Bean
    @Primary
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
