package study.data_querydsl_jpa.repository;

public interface UsernameOnly {

    //@Value("#{target.username + ' ' + target.age}")
    String getUsername();
}
