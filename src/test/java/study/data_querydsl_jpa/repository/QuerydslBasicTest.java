package study.data_querydsl_jpa.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.data_querydsl_jpa.entity.QMember.member;
import static study.data_querydsl_jpa.entity.QTeam.team;

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

    /**
     * 반환 타입
     * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환 fetchOne() : 단 건 조회
     * 결과가 없으면 : null
     * 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException fetchFirst() : limit(1).fetchOne()
     * fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행 fetchCount() : count 쿼리로 변경해서 count 수 조회
     */
    @Test
    public void resultFetch() throws Exception {
        //given
        //when

        //list 조회
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        //단 건
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                )
                .fetchOne();

        //처음 한 건 조회
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();
        //fetchFirst() 같음 -> limit(1).fetchOne();

        //공식 홈페이지 에서는 fetchResults 보다 fetch 를 권장 하고있음. ->복잡한 쿼리에서는 데이터가 다를 수 있다.
        //페이징 에서 사용
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        //fetch 를 사용을 권장함 복잡한 쿼리에서는 정상적인 작동을 보장 받을 수 없고, 따로 count 를 구하는 쿼리 생성을 추가.
        //count 쿼리로 변경
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

        //then
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() throws Exception {
        //given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        //then
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void pageing1() throws Exception {
        //given

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        //then
        assertThat(result.size()).isEqualTo(2);
    }

    /**
     * 주의: count 쿼리가 실행되니 성능상 주의!
     * 참고: 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
     * count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두 조인을 해버리기 때문에 성능이 안나올 수 있다.
     * count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면, count 전용 쿼리를 별도로 작성해야 한다.
     *
     * @throws Exception
     */
    @Test
    public void pageing2() throws Exception {
        //given

        //when
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        //then
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이 * from Member m
     */
    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하라.
     */
    @Test
    public void group() throws Exception {
        //given

        //when
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        //groupBy(), having() 예시
        //---
        //... .groupBy(item.price)
        //... .having(item.price.gt(1000))
        //---
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2
    }

    /**
     * 팀A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
//        QMember member = QMember.member;
//        QTeam team = QTeam.team;
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인) * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
}
