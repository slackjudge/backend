package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String slackId;

    @Column(nullable = false)
    private String baekJoonId;

    @Column(nullable = false)
    private String username;

    @Column
    @Enumerated(EnumType.STRING)
    private EurekaTeamName teamName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false)
    private Integer solvedCount;

    @Column(nullable = false)
    private boolean isAlertAgreed;

    @Column(nullable = false)
    private boolean isDeleted;
}
