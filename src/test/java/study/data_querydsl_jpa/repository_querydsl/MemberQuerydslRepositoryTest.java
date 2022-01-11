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

    /**
     * 아래 기술 되는 기능은 스프링 JPA 에서 제공은 하지만 조인되거나 조금만 복잡해줘도 사용이 힘들다.
     * 그냥 편하게 보시면 될 듯 하다.
     *
     * QuerydslPredicateExecutor - > jpaRepository 가 제공하는 함수들을 좀더 편하고 구체적으로 쓸 수 있게 도와준다.
     * -> 상속을 하나 받아야 하고, left join 이 지원이 되지 않는다. 예제 코드 X 혼란만 가중시킴.
     * 클라이언트가 querydsl 에 의존한다.
     *
     * Querydsl Web 지원
     * -> 기술은 좋아보이나 QuerydslPredicateExecutor 마찬가지로 left join 이 안되고, 부가적인 기능도 equal 정보만 제공하며,
     * 한계가 명확하여 예제 코드 및 적용은 하지 않는다.
     * 컨트롤러가 querydsl 에 의존한다.
     *
     * QuerydslRepositorySupport (추상 클래스) -> 사용 예시.
     * 1.사용하려는 repository 클래스로 가서 상속을 받는다. ex) MemberQueryRepositoryImpl -> extends QuerydslRepositorySupport
     * 2.의존성 받는 부분의 코드를 주석처리하고 constructor matching super 를 생성한다.
     * ex)  public MemberQuerydslRepositoryImpl() {
     *         super(Member.class);
     *     }
     * 여러 가지 기능을 제공을 하는데, EntityManager 도 직접 받기 때문에 사용 할 수 있고, 제공되는 Util 성의 QueryDsl 등의 기능을 제공한다
     * -> 사용하려면 search 메서드의 구문이 변경이 되는데 from 부터 시작해야 하는 단점이 있다.(from.where.select.fetch)
     * -> 예전 버전에서 from 으로 시작해서 그렇고, 그 당시 QueryFactory 가 나오기 전이였다.
     * -> 엔티티매니저, 등 대신 주입을 받아주고 편리한 점도 있고, 페이징을 편리하게 제공해 준다.(offset, limit 등 대신 해줌)
     * 단점 : sort 기능이 정상 동작하지 않음. QueryFactory 를 제공하지 않음.
     */

    /*
     * querydsl 지원 클래스 직접 만들기. ( QuerydslRepositorySupport 의 상위호횐 개념이랄까...)
     * 목표 :
     * 1.스프링 데이터가 제공하는 페이징을 편리하게 변환
     * 2.페이징과 카운트 쿼리 분리 가능하게
     * 3.스프링 데이터 sort 지원
     * select(), selectFrom() 으로 시작 가능하게.
     * EntityManager, QueryFactory 제공.
     * */


}























