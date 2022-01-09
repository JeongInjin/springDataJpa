package study.data_querydsl_jpa.repository_querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.data_querydsl_jpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.data_querydsl_jpa.entity.QMember.member;

/**
 * @RequiredArgsConstructor 식으로 사용하려면 application starter 에
 * @Bean JPAQueryFactory jpaQueryFactory(EntityManager em) {
 * return new JPAQueryFactory(em);
 * }
 * 식으로 선언해주면된다.
 * -> test 코드 짤 때 조금 귀찮아 진다. 주입받아야 할것이 2개 이기 때문에.
 * <p>
 * 참고: 동시성 문제는 걱정하지 않아도 된다.
 * 왜냐하면 여기서 스프링이 주입해주는 엔티티 매니저는 실제 동작 시점에 진짜 엔티티 매니저를 찾아주는 프록시용 가짜 엔티티 매니저이다.
 * 이 가짜 엔티티 매니저는 실제 사용 시점에 트랜잭션 단위로 실제 엔티티 매니저(영속성 컨텍스트)를 할당해준다.
 */
@Repository
public class MemberQuerydslJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberQuerydslJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    /**
     * Querydsl 추가
     *
     * @return
     */
    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member).fetch();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
}