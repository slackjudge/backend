package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name="users_problem")
@Getter
@ToString
@Entity
public class UsersProblemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="users_problem_id")
    private Long usersProblemId;


    /**
     * 문제 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="problem_id", nullable = false)
    private ProblemEntity problem;

    /**
     * 유저 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;

    @Column(name="is_solved", nullable = false)
    private boolean isSolved;

    @Column(name="solved_time")
    private LocalDateTime solvedTime;
}
