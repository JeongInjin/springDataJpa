package study.data_querydsl_jpa.repository_querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;
import study.data_querydsl_jpa.dto.QMemberTeamDto;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.repository_querydsl.support.Querydsl4RepositorySupport;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.data_querydsl_jpa.entity.QMember.member;
import static study.data_querydsl_jpa.entity.QTeam.team;

/**
 * Querydsl4RepositorySupport 상속받아 사용
 */
@Repository
public class MemberTestRepositoryBySupport extends Querydsl4RepositorySupport {

//    public MemberTestRepositoryBySupport(Class<?> domainClass) {
//        super(domainClass);
//    }

    public MemberTestRepositoryBySupport() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    /**
     * 기존 querydsl support 방식
     */
    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl().applyPagination(pageable, query)
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    /**
     * support 방식 - 1차 -> 카운트쿼리도 같이 한번에
     * TODO : Entity 를 조회하고 -> stream().map() 형식으로 리스트를 변환하는게 나을지..dto 를 조회하는게 나을지..성능 찾아봐야 함..
     * -> Entity 조회말고 dto 로 가져오는 것으로 변경 함.
     * -> 쿼리 처럼 *(모든 갓 :별표) 가 없는가..찾아 봐야겠다.
     */
    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {

//        return applyPagination(pageable, query -> query
//                .selectFrom(member)
//                .leftJoin(member.team, team)
//                .where(
//                        usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())
//                )
//        );

        return applyPagination(pageable, query -> query
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
        );
    }

    /**
     * support 방식 - 2차 -> 카운트 쿼리 최적화
     * 현재 v6 는 스택오버플로우 익셉션 발생중, Member Entity 에 JsonIgnore 적용하면 문제는 없긴한데 다른 방법 찾는 중.
     */
    public Page<MemberTeamDto> applyPaginationImproved(MemberSearchCondition condition, Pageable pageable) {

        return applyPagination(pageable, contentQuery -> contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )

                , countQuery -> countQuery
                        .select(member.id)
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
    }

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
