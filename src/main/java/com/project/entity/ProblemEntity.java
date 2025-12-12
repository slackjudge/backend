package com.project.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;


@Entity
@Table(name = "problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProblemEntity {

    /**
     * 문제 번호 range: 1000 ~ 34889
     */
    @Id
    @Column(name = "problem_id", nullable = false)
    private Integer problemId;

    /**
     * 문제 레벨 0 ~ 30
     */
    @Column(name = "problem_level", nullable = false)
    @Min(0)
    @Max(30)
    private Integer problemLevel;

    /**
     * 문제 url : https://www.acmicpc.net/problem/34876
     */
    @Column(name = "problem_url", nullable = false)
    private String problemUrl;

    /**
     * 문제 이름 : 파티로 가는 길
     */
    @Column(name = "problem_title", nullable = false)
    private String problemTitle;
}
