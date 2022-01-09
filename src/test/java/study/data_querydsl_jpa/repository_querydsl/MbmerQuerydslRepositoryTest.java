package study.data_querydsl_jpa.repository_querydsl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MbmerQuerydslRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MbmerQuerydslRepository mbmerQuerydslRepository;

    @Test
    public void basicTest() {
        //given
        Member member = new Member("member1", 10);
        mbmerQuerydslRepository.save(member);

        //when
        Optional<Member> findMember = mbmerQuerydslRepository.findById(member.getId());
        List<Member> result1 = mbmerQuerydslRepository.findAll();
        List<Member> result2 = mbmerQuerydslRepository.findByUsername(member.getUsername());

        //then
        assertThat(findMember.isEmpty()).isFalse();
        assertThat(findMember.get()).isEqualTo(member);

        assertThat(result1).containsExactly(member);

        assertThat(result2).containsExactly(member);
    }
}