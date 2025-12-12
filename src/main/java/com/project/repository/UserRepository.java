package com.project.repository;

import com.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>  {

    Optional<UserEntity> findBySlackId(String slackId);

    Optional<UserEntity> findByUsername(String username);

}
