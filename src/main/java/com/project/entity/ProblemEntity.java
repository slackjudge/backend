package com.project.entity;


import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name="problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemEntity {

    @Id
    @Column(name="problem_id", nullable = false)
    private Integer problemId;

    @Column(name="problem_level", nullable = false)
    private Integer problemLevel;

    @Column(name="problem_url", nullable = false)
    private String problemUrl;

    @Column(name="problem_title", nullable = false)
    private String problemTitle;
}
