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

    private Long userId;
    private int rank;
    private int tier;
    private String name;
    private int totalScore;
    private long solvedCount;
    private String baejoonId;
    private String team;
    private int diff;

    public RankingRowResponse(Long userId, String name, int tier, int totalScore, String baejoonId, String team) {
        this.userId = userId;
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.baejoonId = baejoonId;
        this.team = team;
        this.rank = 0;
        this.diff = 0;
    }
}
