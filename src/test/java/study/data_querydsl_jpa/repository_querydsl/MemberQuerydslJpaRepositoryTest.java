package study.data_querydsl_jpa.repository_querydsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.dto.MemberSearchCondition;
import study.data_querydsl_jpa.dto.MemberTeamDto;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA & Querydsl..
 */
@SpringBootTest
@Transactional
class MemberQuerydslJpaRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberQuerydslJpaRepository memberQuerydslJpaRepository;

    @BeforeEach
    public void init() {
        em.createQuery("delete from Member m").executeUpdate();
        em.createQuery("delete from Team t").executeUpdate();
    }

    @BeforeEach
    public void before() {

        em.createQuery("delete from Member m").executeUpdate();
        em.createQuery("delete from Team t").executeUpdate();

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
    }

    @Test
    public void basicTest() {
        //given
        Member member = new Member("member1", 10);
        memberQuerydslJpaRepository.save(member);

        //when
        Optional<Member> findMember = memberQuerydslJpaRepository.findById(member.getId());
        List<Member> result1 = memberQuerydslJpaRepository.findAll();
        List<Member> result2 = memberQuerydslJpaRepository.findByUsername(member.getUsername());

        //then
        assertThat(findMember.isEmpty()).isFalse();
        assertThat(findMember.get()).isEqualTo(member);

        assertThat(result1).containsExactly(member);

        assertThat(result2).containsExactly(member);
    }

    @Test
    public void basicQuerydslTest() throws Exception {
        //given
        Member member = new Member("member1", 10);
        memberQuerydslJpaRepository.save(member);

        //when
        Optional<Member> findMember = memberQuerydslJpaRepository.findById(member.getId());
        List<Member> result1 = memberQuerydslJpaRepository.findAll_Querydsl();
        List<Member> result2 = memberQuerydslJpaRepository.findByUsername_Querydsl(member.getUsername());

        //then
        assertThat(findMember.isEmpty()).isFalse();
        assertThat(findMember.get()).isEqualTo(member);

        assertThat(result1).containsExactly(member);

        assertThat(result2).containsExactly(member);
    }

    /**
     * 동적 쿼리 짤때 주의점은 반드시 조건이 있거나, 또는 페이징 처리가 반드시 들어가야 한다.
     *
     * @throws Exception
     */
    @Test
    public void searchByBuilderTest() throws Exception {
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
        List<MemberTeamDto> result = memberQuerydslJpaRepository.searchByBuilder(condition);
        List<MemberTeamDto> result2 = memberQuerydslJpaRepository.searchByBuilder(condition2);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
        assertThat(result2).extracting("username").containsExactly("member3", "member4");
    }

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
        List<MemberTeamDto> result = memberQuerydslJpaRepository.search(condition);
        List<MemberTeamDto> result2 = memberQuerydslJpaRepository.search(condition2);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
        assertThat(result2).extracting("username").containsExactly("member3", "member4");
    }

}

























