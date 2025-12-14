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
    private int rank; // 서비스에서 계산
    private int tier;
    private String name;
    private int totalScore;
    private long solvedCount;
    private String baekjoonId;
    private String team;
    private int diff; // 서비스에서 계산

    /**
     * db 조회용 생성자
     */
    public RankingRowResponse(Long userId, String name, int tier, int totalScore, long solvedCount, String baekjoonId, String team) {
        this.userId = userId;
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.solvedCount = solvedCount;
        this.baekjoonId = baekjoonId;
        this.team = team;
        // DB 조회 시점에서는 랭킹 계산을 하지 않기에 0으로 초기화
        this.rank = 0;
        this.diff = 0;
    }
}
