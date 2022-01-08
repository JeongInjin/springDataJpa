package study.data_querydsl_jpa.dto;

import lombok.Data;
import study.data_querydsl_jpa.entity.Member;

@Data
public class MemberDto {

    private Long id;
    private String userName;
    private String teamName;

    public MemberDto(Long id, String userName, String teamName) {
        this.id = id;
        this.userName = userName;
        this.teamName = teamName;
    }

    public MemberDto(Member member) {
        this.id = member.getId();
        this.userName = member.getUsername();
    }
}
