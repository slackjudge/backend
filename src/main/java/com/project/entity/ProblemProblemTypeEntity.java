package com.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * {문제 유형 번호, 문제 번호}를 유니크 제약 조건으로 설정
 * 문제 유형 번호를 기준으로 인덱스를 생성 -> 조회 성능
 */
@Table(
        name = "problem_problem_type",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = " uk_problem_type_problem",
                        columnNames = {"problem_type_id", "problem_id"}
                        )
        },
        indexes = {
                @Index(
                        name = "idx_problem_type",
                        columnList = "problem_type_id"
                )
        }
)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemProblemTypeEntity {

    /**
     * PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_problem_type_id")
    private Long problemProblemTypeId;

    /**
     * 문제 유형 번호 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_type_id", nullable = false)
    private ProblemTypeEntity problemType;


    /**
     * 문제 번호 - FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;
}
