package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder.Default
    @Column(nullable = false)
    private String slackId = "initial";

    @Builder.Default
    @Column(nullable = false)
    private String baekJoonId = "initial";

    @Builder.Default
    @Column(nullable = false)
    private String username = "initial";

    @Builder.Default
    @Column
    @Enumerated(EnumType.STRING)
    private EurekaTeamName teamName = EurekaTeamName.DEFAULT;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    @Builder.Default
    @Column(nullable = false)
    private Integer solvedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAlertAgreed = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;
}
