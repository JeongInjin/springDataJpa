package study.data_querydsl_jpa.repository;

import org.springframework.stereotype.Repository;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class TeamJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Team save(Team team) {
        em.persist(team);

        return team;
    }

    public void delete(Team team) {
        em.remove(team);
    }

    public List<Team> findAll() {
        return em.createQuery("select t from Team t", Team.class)
                .getResultList();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public Optional<Team> findById(Long id) {
        Team team = em.find(Team.class, id);

        return Optional.ofNullable(team);
    }

    public Long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult();
    }

}
