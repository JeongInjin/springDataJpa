package study.data_querydsl_jpa.repository_querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;
import study.data_querydsl_jpa.dto.QMemberTeamDto;
import study.data_querydsl_jpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.data_querydsl_jpa.entity.QMember.member;
import static study.data_querydsl_jpa.entity.QTeam.team;

/**
 * 조건 : MemberQueryRepository 를 사용 할 것이기 때문에 MemberQueryRepository + Impl 이라는 이름으로 만들어 주어야 한다.
 */
public class MemberQuerydslRepositoryImpl implements MemberQuerydslRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = content.size();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /**
         * fetchResults(), fetchCount() 의 2가지 방법으로 페이징 처리를 하려하니, 둘다 Deprecated(사용되지 않음) 경고가 발생.
         * fetchResults 로 페이징과 동시에 count 를 가져오고, 혹은
         * fetchCount 로 카운트 쿼리를 따로 호출하려했는데, 현재 최적화 하는 방법을 찾는중.
         * 내 생각에는 그냥 fetch() 호출하여 페이징 처리된 데이터, 토탈카운트는 최적 쿼리를 짜서 fetch() 후, size() 로 가져와야 할 것 같다.
         */
        List<Member> count = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
        long total = count.size();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 스프링 데이터 라이브러리가 제공
     * count 쿼리가 생략 가능한 경우 생략해서 처리
     * 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
     * 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
     */
    public Page<MemberTeamDto> searchPageComplexImproved(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //fetch() 하기전에는 실제로 쿼리가 날라가진 않는다.
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

//        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
        return PageableExecutionUtils.getPage(content, pageable, countQuery.fetch()::size);
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
