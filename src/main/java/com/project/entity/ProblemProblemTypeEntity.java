package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Table(
        name="problem_problem_type",
        uniqueConstraints ={
                @UniqueConstraint(
                        name =" uk_problem_type_problem",
                        columnNames = {"problem_type_id", "problem_id"}
                        )
        },
        indexes = {
                @Index(
                        name ="idx_problem_type",
                        columnList = "problem_type_id"
                )
        }
)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemProblemTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="problem_problem_type_id")
    private Long problemProblemTypeId;

    /**
     * 문제 유형 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="problem_type_id", nullable = false)
    private ProblemTypeEntity problemType;


    /**
     * 문제 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="problem_id", nullable = false)
    private ProblemEntity problem;
}
