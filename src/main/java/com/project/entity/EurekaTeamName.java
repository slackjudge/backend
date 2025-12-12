package com.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EurekaTeamName {
  FRONTEND_FACE("프론트엔드 대면반"),
  FRONTEND_NON_FACE("프론트엔드 비대면반"),
  BACKEND_FACE("백엔드 대면반"),
  BACKEND_NON_FACE("백엔드 비대면반");

  private final String description;
}
