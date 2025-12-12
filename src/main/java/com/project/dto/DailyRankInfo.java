package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyRankInfo {
    private String name;
    private int solved;
    private int score;
    private int rank;
}
