package study.data_querydsl_jpa.dto;

import lombok.Data;

@Data
public class UserDslDto {

    private String name;
    private int age;

    public UserDslDto() {
    }

    public UserDslDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

