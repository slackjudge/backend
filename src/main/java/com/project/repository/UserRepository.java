package com.project.repository;

import com.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findBySlackId(String slackId);

    Optional<UserEntity> findByUsername(String username);

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM UserEntity u
        WHERE u.baekjoonId = :baekjoonId
          AND u.isDeleted = false
    """)
    boolean existsByBaekjoonId(@Param("baekjoonId") String baekjoonId);

}
