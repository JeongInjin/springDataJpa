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
import static study.data_querydsl_jpa.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

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
    public void startQuerydsl() throws Exception {
        //given
        //기본 Q-Type 활용의 한가지 방식으로 alias 를 직접 줘서 'm' 을 통해 데이터에 접근한다.
        //아래 쪽에서 static import 와, Qmember 안에 지정된 형식을 쓰는 권장 방식을 테스트 케이스로 남겨놓았다.
        //아래와 같이 직접 alias 를 이용하요 사용할때 예시는..같은테이블을 join 할때 사용한다.
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

    /**
     * startQuertdsl 를 static Import 형식으로 리펙터링.
     * Qmember 안에 지정된 형식이라면 static import 받아서 사용하는 것을 권장.
     * querydsl 은 결국 jpql 의 builder  역할을 한다.
     * 결과적으로 querydsl 로 작성한 코드는 jqpl 로 변경된다.
     */
    @Test
    public void startQuerydslImprovement() throws Exception {
        //given

        //when
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * member.username.eq("member1")                        // username = 'member1'
     * member.username.ne("member1")                        // username != 'member1'
     * member.username.eq("member1").not()                  // username != 'member1'
     * member.username.isNotNull()                          // 이름이 is not null
     * member.age.in(10, 20)                                // age in (10,20)
     * member.age.notIn(10, 20)                             // age not in (10, 20)
     * member.age.between(10,30)                            // between 10, 30
     * member.age.goe(30)                                   // age >= 30
     * member.age.gt(30)                                    // age > 30
     * member.age.loe(30)                                   // age <= 30
     * member.age.lt(30)                                    // age < 30
     * member.username.like("member%")                      // like 검색
     * member.username.contains("member")                   // like ‘%member%’ 검색
     * member.username.startsWith("member")                 // like ‘member%’ 검색 ...
     * filter :  #검색조건, #search
     */
    @Test
    public void search() throws Exception {
        //given

        //when
        //and 로 chain 형식으로
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                                .and(member.age.eq(10))
                                .and(member.age.between(5, 15))
                )
                .fetchOne();

        //... ',' 로 연결하기
        Member findMember2 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member2"),
                        member.age.eq(20),
                        member.age.goe(20)
                )
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember2.getUsername()).isEqualTo("member2");
    }
}
