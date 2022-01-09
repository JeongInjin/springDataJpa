package study.data_querydsl_jpa.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDslDto {

    private String username;
    private int age;

    public MemberDslDto() {
    }

    @QueryProjection
    public MemberDslDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
