package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * author : 박준희
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RankingRowExtendedResponse {
    private Long userId;
    private int rank;
    private int tier;
    private String name;
    private int totalScore;
    private long solvedCount;
    private String baekjoonId;
    private String team;
    private int diff;

    // 신규유저
    private boolean newUser;

    public RankingRowExtendedResponse(Long userId, String name, int tier, int totalScore, long solvedCount, String baekjoonId, String team, boolean newUser) {
        this.userId = userId;
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.solvedCount = solvedCount;
        this.baekjoonId = baekjoonId;
        this.team = team;
        this.rank = 0;
        this.diff = 0;
        this.newUser = newUser;
    }
}
