package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findHelloBy();

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto( m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username);

    Member findMemberByUsername(String username);

    Optional<Member> findOptinalByusername(String username);

    @Query(value = "select m from Member m", countQuery = "select count(m.username) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    //update 시 @Modifying annotation 필요!
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    List<Member> findByUsername(String member5);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    /********findMemberEntityGraph 는 아래 @Override + EntityGraph 와 동일하게 적용된다*******/
    //    @Override
    //    @EntityGraph(attributePaths = {"team"})
    //    List<Member> findAll();
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메서드 이름 으로 생성 및 EntityGraph 적용
    //회원데이터 쓸때 팀 데이터 쓸일이 많다..할때 쓸만할 듯.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    /********NamedEntityGraph 는 잘 쓰지 않치만 설명을 적어 봅니다.*******/
    // => MemberEntity 에 NamedEntityGraph 참조 와 name 설정 값인 아래 findNamedEntityGraphByUsername (Member.all) 참조
    @EntityGraph("Member.all")
    List<Member> findNamedEntityGraphByUsername(@Param("username") String username);

    //EntityGraph, NamedEntityGraph 는 JPA 표준 스펙으로 제공하는 기능

    /****************************************************************************************/

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadlOnlyByUsername(String username);

    /**
     * jpa 가 제공하는 annotation
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    /**
     * Projections
     */
    List<UsernameOnly> findProjectionByUsername(String username);

    List<UsernameOnlyDto> findProjectionDtoByUsername(String username);

    <T> List<T> findProjectionClassTypeByUsername(String username, Class<T> type);

}
