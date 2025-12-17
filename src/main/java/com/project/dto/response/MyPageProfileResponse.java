package com.project.dto.response;

/*
author : 최하영
*/
public record MyPageProfileResponse(
        String username,
        String baekjoonId,
        int tierLevel,
        Long totalScore
) {
}
