package com.project.dto.response;

public record MyPageProfileResponse(
        String username,
        String baekjoonId,
        int tierLevel,
        Long totalScore
) {
}
