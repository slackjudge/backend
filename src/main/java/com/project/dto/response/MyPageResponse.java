package com.project.dto.response;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class MyPageResponse {
    // 사용자 기본 정보
    private Profile profile;
    private List<Grass> grass;
    private SelectedDateDetail selectedDateDetailDto;

    @Getter
    @Builder
    public static class Profile{
        private String username;
        private String baekjoonId;
        private int tierLevel;
        private Long totalScore;//전체 점수
    }

    @Getter
    @Builder
    public static class Grass{
        private String date; //"2021-09-01"
        private int solvedCount;
    }
    @Getter
    @Builder
    public static class SelectedDateDetail{
        private String date;
        private int dailyScore;
        private int dailyRank;
        private int solvedCount;
        private int maxDifficulty;
        private List<ProblemDto> problems;
    }

    @Getter
    @Builder
    public static class ProblemDto{
        private String title;
        private int tierLevel;
        private String link;
    }
}
