package study.data_querydsl_jpa.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TeamJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    TeamJpaRepository teamJpaRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    public void init() {
        em.createQuery("delete from Member m").executeUpdate();
        em.createQuery("delete from Team t").executeUpdate();
    }


    @Test
    public void teamCRUD() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamJpaRepository.save(teamA);
        teamJpaRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        memberJpaRepository.save(member4);

        Team findTeamA = teamJpaRepository.findById(member1.getTeam().getId()).get();
        Team findTeamB = teamJpaRepository.findById(member3.getTeam().getId()).get();

        assertThat(findTeamA).isEqualTo(member1.getTeam());
        assertThat(findTeamB).isEqualTo(member3.getTeam());

        List<Team> teamList = teamJpaRepository.findAll();
        assertThat(teamList.size()).isEqualTo(2);

        long teamCount = teamJpaRepository.count();
        assertThat(teamCount).isEqualTo(2);

        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);
        memberJpaRepository.delete(member3);
        memberJpaRepository.delete(member4);
        teamJpaRepository.delete(teamA);
        teamJpaRepository.delete(teamB);
        long deleteCount = teamJpaRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }
}