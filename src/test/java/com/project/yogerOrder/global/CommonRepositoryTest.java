package com.project.yogerOrder.global;

import com.project.yogerOrder.global.config.JpaConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
// dataJpaTest에 @Transactional을 포함되어 있어 각 테스트마다 flush가 일어나지 않아 트랜잭션을 분리하도록 설정이 가능하지만,
// 발생하는 예외가 wrapping되어 예상 예외를 return 받기 힘듬
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class CommonRepositoryTest extends UsingTestContainerTest{
}