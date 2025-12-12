package com.project.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class MyPageResponse {
  // 사용자 기본 정보
  private Profile profile;
  private List<Grass> grass;
  private SelectedDateDetail selectedDateDetail;

  @Getter
  @Builder
  public static class Profile {
    private String username;
    private String baekjoonId;
    private int tierLevel;
    private Long totalScore; // 전체 점수
  }

  @Getter
  @Builder
  @AllArgsConstructor // [추가]
  @NoArgsConstructor // [추가]
  public static class Grass {
    private String date; // "2021-09-01"
    private int solvedCount;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SelectedDateDetail {
    private String date;
    private int dailyScore;
    private int dailyRank;
    private int solvedCount;
    private int maxDifficulty;
    private List<Problem> problems;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Problem {
    private String title;
    private int tierLevel;
    private String link;
  }
}
