package com.project.dto.response;

public record BojCheckResponse(
        String baekjoonId,
        boolean isBaekjoonId,
        boolean isUsed
) {
}
