package study.data_querydsl_jpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * PostConstruct 안에 Transactional 아래 코드를 넣어두면 안될까? -> 스프링 라이플 싸이클로 인해 두가지의 애노테이션은 같이 둘 수 없어서 분리를 해주어야 한다.
 */
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        System.out.println("============================================================================================================================================");
        System.out.println("============================================================================================================================================");
        System.out.println("============================================================================================================================================");
        System.out.println("============================================================================================================================================");
        System.out.println("============================================================================================================================================");
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }

        }
    }
}
