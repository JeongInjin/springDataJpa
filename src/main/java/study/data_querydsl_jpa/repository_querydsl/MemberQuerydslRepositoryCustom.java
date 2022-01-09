package study.data_querydsl_jpa.repository_querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;

import java.util.List;

public interface MemberQuerydslRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
    
    Page<MemberTeamDto> searchPageComplexImproved(MemberSearchCondition condition, Pageable pageable);
}
