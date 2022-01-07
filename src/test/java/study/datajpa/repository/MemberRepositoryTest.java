package study.datajpa.repository;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
//@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;

    //Test 전 마다 매번 실행된다.
    @Before("")
    public void initialize() {
        em.flush();
        em.clear();
    }

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() {
        //given
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("BBB", 15);

        //then
        //조회시 없으면 IndexOutOfBoundsException 발생
        //IndexOutOfBoundsException thrown = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15));

        assertThat(result.get(0).getUsername()).isEqualTo("BBB");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy() throws Exception {
        //given
        List<Member> helloBy = memberRepository.findHelloBy();

        //when

        //then
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();

        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
        assertThat(usernameList.get(0)).isEqualTo("AAA");
        assertThat(usernameList.get(1)).isEqualTo("BBB");
    }

    @Test
    public void findMemberDto() {
        //given
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.changeTeam(team);
        memberRepository.save(m1);

        //when
        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }

        //then
        assertThat(memberDto.get(0).getUserName()).isEqualTo("AAA");
        assertThat(memberDto.get(0).getTeamName()).isEqualTo("teamA");

    }

    @Test
    public void findByNames() throws Exception {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        //when
        List<Member> result = memberRepository.findByNames(Arrays.asList(m1.getUsername(), m2.getUsername()));
        for (Member member : result) {
            System.out.println("member = " + member);
        }

        //then
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(10);
        assertThat(result.get(1).getUsername()).isEqualTo("BBB");
        assertThat(result.get(1).getAge()).isEqualTo(20);
    }

    @Test
    public void returnType() throws Exception {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 10);
        Member m3 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);

        //when
        //List<T> => 값이 없이 조회가 되도 null 이 아니라 빈 collection 을 제공해 준다.
        List<Member> listByUsernames = memberRepository.findListByUsername(m1.getUsername());
        List<Member> listByUsernamesIsEmpty = memberRepository.findListByUsername("CCC");
        //단건 조회시에는 null 이 반환되므로 조심하자. 또는 2개 이상시에도 문제가 있다.
        Member memberByUsername = memberRepository.findMemberByUsername(m3.getUsername());
        Member memberByUsernameIsNull = memberRepository.findMemberByUsername("DDD");
        //Optional
        Optional<Member> optinalByusername = memberRepository.findOptinalByusername(m3.getUsername());
        Optional<Member> optinalByusernameIsEmpty = memberRepository.findOptinalByusername("DDD");

        //then
        assertThat(listByUsernames.get(0).getUsername()).isEqualTo("AAA");
        assertThat(listByUsernames.get(1).getUsername()).isEqualTo("AAA");
        assertThat(listByUsernames.size()).isEqualTo(2);
        assertThat(listByUsernamesIsEmpty.size()).isEqualTo(0);

        assertThat(memberByUsername.getUsername()).isEqualTo("BBB");
        //현재 아래 코드를 돌리면 NonUniqResultException 이 발생하지만, spring jpa 는 springFrameWork Exception 으로 변경한다.
        IncorrectResultSizeDataAccessException thorwn = assertThrows(IncorrectResultSizeDataAccessException.class, () ->
                memberRepository.findMemberByUsername(m1.getUsername()));

        assertThat(optinalByusername.get().getUsername()).isEqualTo("BBB");
        assertThat(optinalByusernameIsEmpty.isEmpty()).isTrue();
    }

    /**
     * 스프링 jpa 페이징 처리, Page, Slice, Sort ...
     * TIP : left outer join 같은 경우 totalCount 에 join 할 필요가 없다. where 조건에 예외가 없는한 같을 테니깐..
     * 복잡한 쿼리라던지 성능이 저조하다면 @Query(countQuery) 를 적용을 해보자.(성능 테스트 해봐야 함)
     */
    @Test
    public void paging() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        //아래와 같이 Entity를 그대로 노출시키면 절대 절대 안된다.
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        //Entity -> DTO 변환
        page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void paging_slice() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent();
//        long totalElements = page.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(content.size()).isEqualTo(3);
//        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
//        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }
}