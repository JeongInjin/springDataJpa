package study.data_querydsl_jpa.repository_querydsl;

import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;

import java.util.List;

public interface MemberQuerydslRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
