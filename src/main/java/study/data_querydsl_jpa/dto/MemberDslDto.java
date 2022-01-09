package study.data_querydsl_jpa.dto;

import lombok.Data;

@Data
public class MemberDslDto {

    private String username;
    private int age;

    public MemberDslDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
