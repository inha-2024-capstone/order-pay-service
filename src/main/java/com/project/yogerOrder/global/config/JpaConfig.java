package com.project.yogerOrder.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 테스트 시에 관련 bean을 로드하지 않기 때문에, 에러 발생 가능성이 있기에 메인에 적용하지 않음
public class JpaConfig {
}
