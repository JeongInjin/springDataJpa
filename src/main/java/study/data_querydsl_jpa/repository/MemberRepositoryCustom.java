package study.data_querydsl_jpa.repository;

import study.data_querydsl_jpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
