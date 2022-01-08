package study.data_querydsl_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.data_querydsl_jpa.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
