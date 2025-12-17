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

/**
 * author : 박준희
 */
@Entity
@Table(name = "problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@lombok.Builder
public class ProblemEntity {

    @Id
    @Column(name = "problem_id", nullable = false)
    private Integer problemId;

    @Column(name = "problem_level", nullable = false)
    @Min(0)
    @Max(30)
    private Integer problemLevel;

    @Column(name = "problem_url", nullable = false)
    private String problemUrl;

    @Column(name = "problem_title", nullable = false)
    private String problemTitle;
}
