package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyRankRawData {
    private Long userId;
    private String username;
    private Long solvedCount;
    private Long score;
}
