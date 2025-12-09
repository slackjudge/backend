package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name="problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemEntity  extends BaseTimeEntity {

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
