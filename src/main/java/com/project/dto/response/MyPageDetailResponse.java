package com.project.dto.response;

import java.util.List;

/*
author : 최하영
*/
public record MyPageDetailResponse(
        String date,
        int dailyScore,
        int dailyRank,
        int solvedCount,
        int maxDifficulty,
        List<ProblemResponse> problems
) {
}
