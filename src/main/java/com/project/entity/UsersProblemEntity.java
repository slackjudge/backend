package com.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;


/**
 * author : 박준희
 */
@Table(
        name = "users_problem",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_problem", columnNames = {"user_id", "problem_id"})
        }
)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersProblemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_problem_id")
    private Long usersProblemId;

    @Embedded
    private UserProblemRef ref;

    @Column(name = "is_solved", nullable = false)
    private boolean isSolved = false;

    @Column(name = "solved_time")
    private LocalDateTime solvedTime;


    @Builder
    public UsersProblemEntity(UserEntity user, ProblemEntity problem, boolean isSolved, LocalDateTime solvedTime) {
        this.ref = new UserProblemRef(user, problem);
        this.isSolved = isSolved;
        this.solvedTime = solvedTime;
    }
}
