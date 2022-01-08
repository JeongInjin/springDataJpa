package study.data_querydsl_jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.entity.Hello;
import study.data_querydsl_jpa.entity.QHello;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataQueryDslJpaApplicationTest {

    @Autowired
    EntityManager em;

    @Test
    void contextLoads() {
        //given
        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory query = new JPAQueryFactory(em);

        //QHello qHello = new QHello("h");
        QHello qHello = QHello.hello;
        
        //when
        Hello result = query
                .selectFrom(qHello)
                .fetchOne();

        //then
        assertThat(result).isEqualTo(hello);
        assertThat(result.getId()).isEqualTo(hello.getId());
    }

}
