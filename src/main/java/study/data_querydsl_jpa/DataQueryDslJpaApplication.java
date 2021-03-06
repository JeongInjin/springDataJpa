package study.data_querydsl_jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

@EnableJpaAuditing //해당 애노테이션을 넣어야지 Auditing 기능을 jpa data jpa 에서 정상적으로 작동 한다.
@SpringBootApplication
public class DataQueryDslJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataQueryDslJpaApplication.class, args);
    }

    //스프링 Bean 에 JPAQueryFactory를 등록한다
    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    //interface 에서 method 가 하나면 람다형식으로 변경 가능하다.
    //등록 및 수정 될때마다 AuditorAware 를 호출하여 자동으로 값을 채워 넣어 준다.
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(UUID.randomUUID().toString());
    }

    //위 람다 형식 anonymous 형태로 구현 편한거 쓰면 됨.
//    @Bean
//    public AuditorAware<String> auditorProviderAnonymous() {
//        return new AuditorAware<String>() {
//            @Override
//            public Optional<String> getCurrentAuditor() {
//                return Optional.of(UUID.randomUUID().toString());
//            }
//        };
//    }
}
