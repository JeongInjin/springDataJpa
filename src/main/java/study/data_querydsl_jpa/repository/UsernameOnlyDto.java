package study.data_querydsl_jpa.repository;

import lombok.Getter;

@Getter
public class UsernameOnlyDto {

    private final String username;

    public UsernameOnlyDto(String username) {
        this.username = username;
    }
}
