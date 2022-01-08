package study.data_querydsl_jpa.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.QMember;
import study.data_querydsl_jpa.entity.Team;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() throws Exception {
        //given
        //member1을 찾자.

        //when
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 컴파일 시점에 오류를 발견할 수 있고, 바인딩을 해주고, 편의기능을 제공해준다.
     */
    @Test
    public void startQuertdsl() throws Exception {
        //given
        queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");

        //when
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
