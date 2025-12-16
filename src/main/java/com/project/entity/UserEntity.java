package com.project.entity;

import com.project.common.BaseTimeEntity;
import com.project.dto.request.LocalSignRequest;
import com.project.dto.request.SignUpRequest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private static final String DEFAULT_STRING = "initial";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Builder.Default
    @Column(nullable = false)
    private String slackId = DEFAULT_STRING;

    @Builder.Default
    @Column(nullable = false)
    private String baekjoonId = DEFAULT_STRING;

    @Builder.Default
    @Column(nullable = false)
    private String username = DEFAULT_STRING;

    @Builder.Default
    @Column(nullable = false)
    private int bojTier = 0;

    @Column
    @Enumerated(EnumType.STRING)
    private EurekaTeamName teamName;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalSolvedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAlertAgreed = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    public static UserEntity createUser(String slackId) {
        return UserEntity.builder()
                .slackId(slackId)
                .build();
    }

    public void localSignUp(LocalSignRequest localSignRequest, int bojTier) {
        this.slackId = "local";
        this.username = localSignRequest.username();
        this.baekjoonId = localSignRequest.baekjoonId();
        this.teamName = localSignRequest.teamName();
        this.isAlertAgreed = localSignRequest.isAlertAgreed();
        this.bojTier = bojTier;
    }

    public void signUp(SignUpRequest signUpRequest, int bojTier) {
        this.username = signUpRequest.username();
        this.baekjoonId = signUpRequest.baekjoonId();
        this.teamName = signUpRequest.teamName();
        this.isAlertAgreed = signUpRequest.isAlertAgreed();
        this.bojTier = bojTier;
    }

    public void updateAlertAgreed(boolean isAlertAgreed) {
        this.isAlertAgreed = isAlertAgreed;
    }
}
