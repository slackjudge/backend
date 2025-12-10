package com.project.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Builder
@Table(name = "users_problem")
@Getter
@Entity
public class UsersProblemEntity {

    /**
     * PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_problem_id")
    private Long usersProblemId;


    /**
     * 문제 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;


    /**
     * 유저 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    /**
     * 문제 풀이 유무 : 초기값 false -> 첫 회원가입 후, 첫 배치때 푼 문제 true 전환
     */
    @Builder.Default
    @Column(name = "is_solved", nullable = false)
    private boolean isSolved = false;


    /**
     * 문제 푼 시간 : 1시간 단위로 업데이트
     */
    @Column(name = "solved_time")
    private LocalDateTime solvedTime;
}
