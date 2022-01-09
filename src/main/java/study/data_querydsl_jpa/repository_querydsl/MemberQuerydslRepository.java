package study.data_querydsl_jpa.repository_querydsl;

import org.springframework.data.jpa.repository.JpaRepository;
import study.data_querydsl_jpa.entity.Member;

import java.util.List;

/**
 * 순수 JPA 코드들을 스프링 데이터 JPA 리포지토리로 변경
 */
public interface MemberQuerydslRepository extends JpaRepository<Member, Long>, MemberQuerydslRepositoryCustom {

    List<Member> findByUsername(String username);
}
