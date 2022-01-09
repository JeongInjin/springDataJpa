package study.data_querydsl_jpa.repository_querydsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.entity.Member;

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
}