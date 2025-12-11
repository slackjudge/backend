package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingRowResponse {

    private int rank;
    private String tier;
    private String name;
    private int totalScore;
    private long solvedCount;
    private String baejoonId;
    private String team;
    private int diff;

    public RankingRowResponse(String name, String tier, int totalScore, long solvedCount, String baejoonId, String team) {
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.solvedCount = solvedCount;
        this.baejoonId = baejoonId;
        this.team = team;
    }
}
