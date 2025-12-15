package com.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;



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

    /**
     * PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_problem_id")
    private Long usersProblemId;

    /**
     * 복합 속성 (user_id, problem_id)
     */
    @Embedded
    private UserProblemRef ref;


    /**
     * 문제 풀이 유무 : 초기값 false -> 첫 회원가입 후, 첫 배치때 푼 문제 true 전환
     */
    @Column(name = "is_solved", nullable = false)
    private boolean isSolved = false;


    /**
     * 문제 푼 시간 : 1시간 단위로 업데이트
     */
    @Column(name = "solved_time")
    private LocalDateTime solvedTime;


    @Builder
    public UsersProblemEntity(UserEntity user, ProblemEntity problem, boolean isSolved, LocalDateTime solvedTime) {
        this.ref = new UserProblemRef(user, problem);
        this.isSolved = isSolved;
        this.solvedTime = solvedTime;
    }
}
