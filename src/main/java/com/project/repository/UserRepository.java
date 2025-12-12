package com.project.repository;

import com.project.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findBySlackId(String slackId);

  Optional<UserEntity> findByUsername(String username);
}
