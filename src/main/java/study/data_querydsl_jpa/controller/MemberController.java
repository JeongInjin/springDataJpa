package study.data_querydsl_jpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.data_querydsl_jpa.dto.MemberDto;
import study.data_querydsl_jpa.entity.Member;
import study.data_querydsl_jpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("member" + i, i));
        }
    }

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        //Optinal을 이용하자..
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    /**
     * 도메인 클래스 컨버터
     * 권장하지는 않은 듯..
     * 정말 간단한 쿼리 정도에서만 사용..
     * 반드시!! '조회용' 으로만 사용한다.
     */
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {

        return member.getUsername();
    }

    /**
     * page, size, sort
     * http://localhost:8080/members?page=1&size=3&sort=id,desc&sort=username,desc
     * default = 20 row 입니다.
     * 기본 설정방법 :
     * global -> application.yml -> data.web.pageable.default-page-size : 10, max-page-size: 2000
     * 계속 강조하지마 Entity 를 그대로 반환하지 않고 DTO 를 변환하여 반환한다.
     *
     * @return
     */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5, sort = "username") Pageable pageable) {
        //Page<Member> page = memberRepository.findAll(pageable);
        //Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        //return map;

        // -> inLine style
        //return memberRepository.findAll(pageable)
        //        .map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //생성자 추가로 문법 변경
        //return memberRepository.findAll(pageable)
        //        .map(member -> new MemberDto(member));

        // -> 한번더 코드 정리
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }

    /*
     * 페이지를 default 0 이 아닌 1로 시작하고 싶을때는 직접 값을 항당해줘서 parameter 로 넘긴다.
     * */
    @GetMapping("/members2")
    public Page<MemberDto> list2(@PageableDefault(size = 5, sort = "username") Pageable pageable) {
        PageRequest request = PageRequest.of(1, 2);

        return memberRepository.findAll(request)
                .map(MemberDto::new);
    }

    /**
     * 페이지를 default 0 이 아닌 1로 시작하고 싶을때 다른 설정 방법으로는 application.yml 에서 one-indexed-parameters: true 설정해 준다.
     * 한계가 있다. -> pageable 쪽이 데이터가 서로 틀리기 때문에 제대로 된 페이지를 구현하기 힘듦. 그냥 0부터 쓰는게 가장 깔끔하다.
     */
    @GetMapping("/members3")
    public Page<MemberDto> list3(@PageableDefault(size = 5) Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }

}
