package com.project.dto.request;

import com.project.entity.EurekaTeamName;

public record SignUpRequest(
    String username, String baekjoonId, EurekaTeamName teamName, boolean isAlertAgreed
) {
}
