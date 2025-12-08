package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name ="problem_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProblemTypeEntity extends BaseTimeEntity {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="problem_type_id")
    private Long problemTypeId;

    /**
     * 구현, 그래프, DP..
     */
    @Column(name="problem_type_name", nullable = false, length = 100)
    private String problemTypeName;


    public static ProblemTypeEntity create(String name) {
        ProblemTypeEntity entity = new ProblemTypeEntity();
        entity.problemTypeName = name;
        return entity;
    }


}
