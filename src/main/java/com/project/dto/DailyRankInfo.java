package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 김경민
 */
@Getter
@AllArgsConstructor
public class DailyRankInfo {
    private String name;
    private long solved;
    private long score;
    private int rank;
}
