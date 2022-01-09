package study.data_querydsl_jpa.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    //회원명, 팀명, 나이(ageGoe, ageLoe)

    private String username;
    private String teamName;
    private Integer ageGoe; //크거나 같거나
    private Integer ageLoe; //직가니 깉가니
}
