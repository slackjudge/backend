package com.project.dto.response;

public record BojUserResponse(
        String handle,
        int tier,
        int solvedCount,
        boolean blocked,
        boolean reverseBlocked
) {
}
