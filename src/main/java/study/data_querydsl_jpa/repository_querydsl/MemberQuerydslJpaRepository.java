package study.data_querydsl_jpa.repository_querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;
import study.data_querydsl_jpa.dto.QMemberTeamDto;
import study.data_querydsl_jpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.data_querydsl_jpa.entity.QMember.member;
import static study.data_querydsl_jpa.entity.QTeam.team;

/**
 * @RequiredArgsConstructor 식으로 사용하려면 application starter 에
 * @Bean JPAQueryFactory jpaQueryFactory(EntityManager em) {
 * return new JPAQueryFactory(em);
 * }
 * 식으로 선언해주면된다.
 * -> test 코드 짤 때 조금 귀찮아 진다. 주입받아야 할것이 2개 이기 때문에.
 * <p>
 * 참고: 동시성 문제는 걱정하지 않아도 된다.
 * 왜냐하면 여기서 스프링이 주입해주는 엔티티 매니저는 실제 동작 시점에 진짜 엔티티 매니저를 찾아주는 프록시용 가짜 엔티티 매니저이다.
 * 이 가짜 엔티티 매니저는 실제 사용 시점에 트랜잭션 단위로 실제 엔티티 매니저(영속성 컨텍스트)를 할당해준다.
 */
//순수 JPA 코드들
@Repository
public class MemberQuerydslJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberQuerydslJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    /**
     * Querydsl 추가
     *
     * @return
     */
    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member).fetch();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    /**
     * 동적쿼리 - Builder 사용
     * 조회조건 추가
     * 동적쿼리는 where 절 추천
     */
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    /**
     * 동적쿼리 - Where 절 사용 - 추천
     */
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    /**
     * 동적쿼리 - Where 절 코드 재사용
     */
    public List<Member> searchMember(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    //Predicate 반환값 보다 BooleanExpression 하는게 더 좋을 듯하다, BooleanExpression 는 and, or 로 체이닝이 가능하다.
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


}

























