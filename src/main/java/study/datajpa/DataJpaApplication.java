package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@EnableJpaAuditing //해당 애노테이션을 넣어야지 Auditing 기능을 jpa data jpa 에서 정상적으로 작동 한다.
@SpringBootApplication
public class DataJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaApplication.class, args);
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
