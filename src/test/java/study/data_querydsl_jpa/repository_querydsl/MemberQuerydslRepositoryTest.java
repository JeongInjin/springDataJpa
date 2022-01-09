package study.data_querydsl_jpa.repository_querydsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberQuerydslRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberQuerydslRepository memberQuerydslRepository;

    @BeforeEach
    public void init() {
        em.createQuery("delete from Member m").executeUpdate();
        em.createQuery("delete from Team t").executeUpdate();
    }

    @Test
    public void basicTest() {
        //given
        Member member = new Member("member1", 10);
        memberQuerydslRepository.save(member);

        //when
        Optional<Member> findMember = memberQuerydslRepository.findById(member.getId());
        List<Member> result1 = memberQuerydslRepository.findAll();
        List<Member> result2 = memberQuerydslRepository.findByUsername(member.getUsername());

        //then
        assertThat(findMember.isEmpty()).isFalse();
        assertThat(findMember.get()).isEqualTo(member);

        assertThat(result1).containsExactly(member);

        assertThat(result2).containsExactly(member);
    }

    /**
     * 동적 쿼리 짤때 주의점은 반드시 조건이 있거나, 또는 페이징 처리가 반드시 들어가야 한다.
     * 해당 where 절 사용을 추천한다.
     */
    @Test
    public void searchByWhereTest() throws Exception {
        //given
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        MemberSearchCondition condition2 = new MemberSearchCondition();
        condition2.setTeamName("teamB");

        //when
        List<MemberTeamDto> result = memberQuerydslRepository.search(condition);
        List<MemberTeamDto> result2 = memberQuerydslRepository.search(condition2);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
        assertThat(result2).extracting("username").containsExactly("member3", "member4");
    }

    @Test
    public void searchPageSimple() throws Exception {
        //given
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<MemberTeamDto> result = memberQuerydslRepository.searchPageSimple(condition, pageRequest);

        //then
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}