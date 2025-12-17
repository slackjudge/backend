package com.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * author : 박준희
 */
@Entity
@Table(name = "problem_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProblemTypeEntity {

    @Id
    @Column(name = "problem_type_id")
    private Long problemTypeId;


    @Column(name = "problem_type_name", nullable = false, length = 100)
    private String problemTypeName;

}
