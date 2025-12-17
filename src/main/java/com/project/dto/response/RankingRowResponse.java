package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * author : 박준희
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingRowResponse {

    private Long userId;
    private int rank;
    private int tier;
    private String name;
    private int totalScore;
    private long solvedCount;
    private String baekjoonId;
    private String team;
    private int diff;

    public RankingRowResponse(Long userId, String name, int tier, int totalScore, long solvedCount, String baekjoonId, String team) {
        this.userId = userId;
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.solvedCount = solvedCount;
        this.baekjoonId = baekjoonId;
        this.team = team;
        this.rank = 0;
        this.diff = 0;
    }
}
