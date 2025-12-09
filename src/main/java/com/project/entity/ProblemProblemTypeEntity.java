package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Table(name="problem_problem_type")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemProblemTypeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="problem_type_id")
    private Long problemTypeId;

    /**
     * 문제 유형 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="problemTypeId", nullable = false)
    private ProblemTypeEntity problemType;


    /**
     * 문제 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="problemId", nullable = false)
    private ProblemEntity problem;
}
