package study.data_querydsl_jpa.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.dto.MemberDslDto;
import study.data_querydsl_jpa.dto.QMemberDslDto;
import study.data_querydsl_jpa.dto.UserDslDto;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.QMember;
import study.data_querydsl_jpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;
import java.util.function.Supplier;

import static com.querydsl.jpa.JPAExpressions.select;
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

        em.createQuery("delete from Member m").executeUpdate();
        em.createQuery("delete from Team t").executeUpdate();

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

    /**
     * ON 절을 활용한 조인(JPA 2.1부터 지원) 1. 조인대상필터링
     * 2. 연관관계없는엔티티외부조인
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID = t.id and t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        //given

        //when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * 주의! 문법을 잘 봐야 한다. leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.
     * 일반조인: leftJoin(member.team, team) on조인: from(member).leftJoin(team).on(xxx)
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    //페치 조인 미적용
    @Test
    public void fetchJoin_No() throws Exception {
        //given
        em.flush();
        em.clear();

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //then
        //isLoaded -> findMember.getTeam() 했을 시 -> 이미 로딩된 entity 인지 초기화 안된 entity 인지 알려준다. -> 로딩이 안되었으면 false
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    //페치 조인 적용
    //페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에
    //조회하는 기능이다. 주로 성능 최적화에 사용하는 방법이다.
    @Test
    public void fetchJoin_Use() throws Exception {
        //given
        em.flush();
        em.clear();

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //then
        //isLoaded -> findMember.getTeam() 했을 시 -> 이미 로딩된 entity 인지 초기화 안된 entity 인지 알려준다. -> 로딩이 안되었으면 false
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 서브쿼리
     * --------------------------------------------------------------------------------------
     * from 절의 서브쿼리 한계 ->
     * JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다.
     * 당연히 Querydsl(jqpl 빌더 역할) 도 지원하지 않는다.
     * 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다.(jpa 표준 스펙에서는 select 에서 서브쿼리 지원을 하지않는다.)
     * Querydsl 도 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
     * <p>
     * from 절의 서브쿼리 해결방안 ->
     * 1. 서브쿼리를 join 으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
     * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
     * 3. nativeSQL 을 사용한다.
     * <p>
     * from 절에서 서브쿼리 만들 시 주의 점 :
     * sql 에서는 데이터만 가져오는 것으로 집중하고, view , format 던지 로직은 애플리케이션에서 처리 하는것을 생각해 보자.
     * --------------------------------------------------------------------------------------
     * JPAExpressions 키워드 사용해야하고 현재 코드에 안보이는 이유는 static import 적용
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
//                        JPAExpressions
//                                .select(memberSub.age.max())
//                                .from(memberSub)
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        //then
        assertThat(result).extracting("age").containsExactly(40);
    }

    /**
     * 서브쿼리
     * 나이가 평균 이상인 조회
     */
    @Test
    public void subQueryGoe() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        //then
        assertThat(result).extracting("age").containsExactly(30, 40);
    }

    /**
     * 서브쿼리
     * in 조건으로 10살 초과인 회원 조회
     */
    @Test
    public void subQueryIn() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        //then
        assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then
    }

    /**
     * case when 이러한 것들은 디비에서 불러오는 것 보다..상황에 따라 디비는 최소한의 데이터와 group 화해서 가져오고 애플리케이션 에서 처리 하자.
     */
    @Test
    public void basicCase() throws Exception {
        //given
        //when
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
        //then
    }

    @Test
    public void complexCase() throws Exception {
        //given
        //when
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0 ~ 20살")
                        .when(member.age.between(21, 30)).then("21 ~ 30살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

        //then
    }

    /**
     * 예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
     * 1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
     * 2. 0 ~ 20살 회원 출력
     * 3. 21 ~ 30살 회원 출력
     */
    @Test
    public void orderByCase() throws Exception {
        //given
        //when
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }

        //then
    }

    //상수
    @Test
    public void constant() throws Exception {
        //given
        //when
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        //then
    }

    //문자 더하기
    //ENUM 처리할때도 .stringValue() 를 사용하여 처리할 수 있다.
    @Test
    public void concat() throws Exception {
        //given
        //when
        //{username}_{age}
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }

        //then
    }


    /**
     * 프로젝션 : select 대상 지정
     * - 프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
     * - 프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
     */
    @Test
    public void simpleProjection() throws Exception {
        //given
        //when
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

        //then
    }

    /**
     * tuple 은 package com.querydsl.core 이며
     * 권장하는 방법은 repository 쪽에서만 사용하고 controller, service 쪽에 해당 기술을 노출 시키는건 좋은 방법은 아닌거 같음.
     */
    @Test
    public void tupleProjection() throws Exception {
        //given
        //when
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            int age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }

        //then
    }

    /**
     * 순수 JPA 에서 DTO 를 조회할 때는 new 명령어를 사용해야함
     * DTO 의 package 이름을 다 적어줘야해서 지저분함
     * 생성자 방식만 지원함
     */
    @Test
    public void findDtoBySPQL() throws Exception {
        //given
        //when
        List<MemberDslDto> result = em.createQuery("select new study.data_querydsl_jpa.dto.MemberDslDto(m.username, m.age) from Member m", MemberDslDto.class).getResultList();

        for (MemberDslDto memberDslDto : result) {
            System.out.println("memberDslDto = " + memberDslDto);
        }

        //then
    }

    /**
     * querydsl 은 3가지 방식을 지원한다. - Projections
     * 프로퍼티 접근,
     * 필드 직접
     * 접근 생성자 사용
     */

    /**
     * 1.Setter 방식
     */
    @Test
    public void findDtoQuerydslBySetter() throws Exception {
        //given
        //when
        List<MemberDslDto> result = queryFactory
                .select(Projections.bean(MemberDslDto.class,
                                member.username,
                                member.age
                        )
                )
                .from(member)
                .fetch();

        for (MemberDslDto memberDslDto : result) {
            System.out.println("memberDslDto = " + memberDslDto);
        }

        //then
    }

    /**
     * 2.Field 방식 - getter, setter 가 없어도 field 에 바로 값을 할당 한다.
     */
    @Test
    public void findDtoQuerydslByField() throws Exception {
        //given
        //when
        List<MemberDslDto> result = queryFactory
                .select(Projections.fields(MemberDslDto.class,
                                member.username,
                                member.age
                        )
                )
                .from(member)
                .fetch();

        for (MemberDslDto memberDslDto : result) {
            System.out.println("memberDslDto = " + memberDslDto);
        }

        //then
    }

    /**
     * 3.Constructor 방식 - Consstructor 를 호출하여 값을 매핑한다.
     */
    @Test
    public void findDtoQuerydslByConstructor() throws Exception {
        //given
        //when
        List<UserDslDto> result = queryFactory
                .select(Projections.constructor(UserDslDto.class,
                                member.username,
                                member.age
                        )
                )
                .from(member)
                .fetch();

        for (UserDslDto userDslDto : result) {
            System.out.println("userDslDto = " + userDslDto);
        }

        //then
    }

    /**
     * 3-1.프로퍼티나, 필드 접근 생성 방식에서 이름이 다를 대 해결 방안
     * `ExpressionUtils.as(source.alias)`: 필드나, 서브 쿼리에 벌칭 적용
     * `username.as(:memberName"): 필드에 별칭 적용
     */
    @Test
    public void findDtoQuerydslByConstructor2() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");
        //when
        List<UserDslDto> result = queryFactory
                .select(Projections.fields(UserDslDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        for (UserDslDto userDslDto : result) {
            System.out.println("userDslDto = " + userDslDto);
        }

        //then
    }

    /**
     * 프로젝션과 결과 반환 - @QueryProjection
     * DTO 에 @QueryProjection 붙인뒤, gradle -> other -> compileQuerydsl 실행
     * QMemberDslDto 안에보면 parameter 형식이 지정되어 있어, 컴파일 시점에 타입을 체크해 주는 장점이 있다.
     * constructor 와의 차이점은 컴파일 시점에 오류를 잡아내냐 못하냐의 차이 정도 있다.
     * 단점? 문제점?
     * 이 방법은 컴파일러로 타입을 체크할 수 있으므로 가장 안전한 방법이다. 다만 DTO에 QueryDSL 어노테이션을 유지해야 하는 점과 DTO까지 Q 파일을 생성해야 하는 단점이 있다.
     * ->DTO 에서는 querydsl 의 의존성이 없었는데 @QueryProjection 에 의해 의존성하게 된다. 추후에 querydsl 을 제거되는 상황이면 소스 수정이 불가피 하다.
     * querydsl 에 의존하고 있어서 순수한 DTO 라 보긴 힘들다.
     * 아키텍쳐를 잘 생각해보고, 협의하게 진행을 해 보자.
     */
    @Test
    public void findByQueryProjection() throws Exception {
        //given
        List<MemberDslDto> result = queryFactory
                .select(new QMemberDslDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDslDto memberDslDto : result) {
            System.out.println("memberDslDto = " + memberDslDto);
        }
        //when

        //then
    }

    /**
     * 동적 쿼리 - BooleanBuilder 사용
     * 동적 쿼리를 해결하는 두가지 방식
     * BooleanBuilder
     * Where 다중 파라미터 사용 - 추천 합니다.
     */

    //1.BooleanBuilder 사용
    @Test
    public void dynamicQuery_BolleanBuilder() throws Exception {
        //given
        String usernameParam = "member1";
        Integer ageParam = 10;

        //when
        List<Member> result = searchBooleanBuilder(usernameParam, ageParam);

        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchBooleanBuilder(String usernameCond, Integer ageCond) {

        //BooleanBuilder builder = new BooleanBuilder();
        //방어코드 및 필수로 parameter 가 넘어온다고 가정하면 builder 에 초기값을 줄 수 있다. chaining 가능.
        BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond).and(member.age.eq(ageCond)));
        if (usernameCond != null) builder.and(member.username.eq(usernameCond));
        if (ageCond != null) builder.and(member.age.eq(ageCond));

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    //where 다중 파라미터 사용 - 추천
    @Test
    public void dynamicQuery() throws Exception {
        //given
        String usernameParam = "member1";
        Integer ageParam = 10;

        //when
        List<Member> result = searchWhereParameter(usernameParam, ageParam);

        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchWhereParameter(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                //.where(usernameEq(usernameCond), ageEq(ageCond))
                //.where(nameAndAgeBasicEq(usernameCond, ageCond))
                .where(nameAndAgeEq(usernameCond, ageCond))
                .fetch();
    }

    /*
    아래 방식은 괜찮긴 한데, allEq 시 username 이 null 일경우 체이닝을 걸 수 없다. 해결 방안을 아래에 기술 합니다.
    private Predicate usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private Predicate ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
    */

    //1번
    private BooleanBuilder nameAndAgeBasicEq(String usernameCond, Integer ageCond) {
        return usernameBasicEq(usernameCond).and(ageBasicEq(ageCond));
    }

    private BooleanBuilder usernameBasicEq(String usernameCond) {
        if (usernameCond == null) {
            return new BooleanBuilder();
        } else {
            return new BooleanBuilder(member.username.eq(usernameCond));
        }
    }

    private BooleanBuilder ageBasicEq(Integer ageCond) {
        if (ageCond == null) {
            return new BooleanBuilder();
        } else {
            return new BooleanBuilder(member.age.eq(ageCond));
        }
    }

    //1번 형식을 람다 형식으로 리펙터링
    private BooleanBuilder usernameEq(String usernameCond) {
        return nullSafeBuilder(() -> member.username.eq(usernameCond));
    }

    private BooleanBuilder ageEq(Integer ageCond) {
        return nullSafeBuilder(() -> member.age.eq(ageCond));
    }

    private BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (IllegalArgumentException e) {
            return new BooleanBuilder();
        }
    }

    private BooleanBuilder nameAndAgeEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

}














