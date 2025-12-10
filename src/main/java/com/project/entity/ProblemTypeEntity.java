package com.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "problem_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProblemTypeEntity {

    /**
     * 문제 타입 번호 : 1 ~ 242
     */
    @Id
    @Column(name = "problem_type_id")
    private Long problemTypeId;


    /**
     * 문제 타입 이름 : 스택, 위상 정렬 ...
     */
    @Column(name = "problem_type_name", nullable = false, length = 100)
    private String problemTypeName;

}
