package study.datajpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;

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

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
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

}