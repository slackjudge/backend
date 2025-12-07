package com.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EurekaTeamName {
    FRONTEND_OFFLINE("프론트엔드 대면반"),
    FRONTEND_ONLINE("프론트엔드 비대면반"),
    BACKEND_OFFLINE("백엔드 대면반"),
    BACKEND_ONLINE("백엔드 비대면반");

    private final String description;
}
