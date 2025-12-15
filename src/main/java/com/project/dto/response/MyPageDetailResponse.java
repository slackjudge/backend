package com.project.dto.response;

import java.util.List;

public record MyPageDetailResponse(
        String date,
        int dailyScore,
        int dailyRank,
        int solvedCount,
        int maxDifficulty,
        List<ProblemResponse> problems // ProblemResponse 사용
) {
}
