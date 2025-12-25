package com.project.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * author : 박준희
 */
@Getter
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
    private boolean newUser;

    private RankingRowExtendedResponse(RankingRowResponse base, boolean newUser) {
        Objects.requireNonNull(base, "base must not be null");

        this.userId = base.getUserId();
        this.rank = base.getRank();
        this.tier = base.getTier();
        this.name = base.getName();
        this.totalScore = base.getTotalScore();
        this.solvedCount = base.getSolvedCount();
        this.baekjoonId = base.getBaekjoonId();
        this.team = base.getTeam();
        this.diff = base.getDiff();
        this.newUser = newUser;
    }

    public static RankingRowExtendedResponse from(RankingRowResponse base, boolean newUser) {
        return new RankingRowExtendedResponse(base, newUser);
    }
}
