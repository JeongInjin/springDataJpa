package study.data_querydsl_jpa.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.repository.MemberRepository;
import study.data_querydsl_jpa.repository.TeamRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @BeforeEach
    public void init() {
        em.createQuery("delete from Member m").executeUpdate();
        em.createQuery("delete from Team t").executeUpdate();
    }

    @Test
    public void testEntity() throws Exception {
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

        //초기화
        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        System.out.println("===============================================");
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
        System.out.println("===============================================");
        Team teamC = new Team("teamC");
        em.persist(teamC);
        member1.changeTeam(teamC);
        memberRepository.save(member1);

        em.flush();
        em.clear();
        Optional<Member> findMember = memberRepository.findById(member1.getId());

        assertThat(findMember.get().getUsername()).isEqualTo("member1");
        assertThat(findMember.get().getTeam().getName()).isEqualTo("teamC");

    }

    @Test
    public void JpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("member1", 10);
        memberRepository.save(member); //@PrePersist

        Thread.sleep(100);
        member.changeName("member2");

        em.flush();
        em.clear();

        //when
        Optional<Member> findMember = memberRepository.findById(member.getId());

        //then
        System.out.println("==================================================================");
        System.out.println("findMember.CreatedDate = " + findMember.get().getCreatedDate());
        System.out.println("findMember.UpdatedDate = " + findMember.get().getLastModifiedDate());
        System.out.println("findMember.CreatedBy = " + findMember.get().getCreatedBy());
        System.out.println("findMember.LastUpdatedBy = " + findMember.get().getLastModifiedBy());
        System.out.println("==================================================================");

        assertThat(findMember.isPresent()).isTrue();
        assertThat(findMember.get().getUsername()).isEqualTo("member2");

    }

    @Test
    @Rollback(value = false)
    public void jpa() throws Exception {
        //given
        Team team = new Team("teamTest1");
        teamRepository.save(team);

        Member member = new Member("testMember1", 10, team);
        memberRepository.save(member);

        //when
        List<Member> result = memberRepository.findByUsername("testMember1");
        for (Member m : result) {
            System.out.println("member = " + m.getTeam());
        }
        Team teamResult = teamRepository.getById(team.getId());

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTeam().getName()).isEqualTo("teamTest1");
        assertThat(result.get(0).getUsername()).isEqualTo("testMember1");
        assertThat(result.get(0).getAge()).isEqualTo(10);
        assertThat(teamResult.getName()).isEqualTo("teamTest1");
        assertThat(teamResult.getId()).isEqualTo(1);
    }

}