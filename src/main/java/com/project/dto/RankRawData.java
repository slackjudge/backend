package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 김경민
 */
@Getter
@AllArgsConstructor
public class RankRawData {
    private Long userId;
    private String username;
    private Long solvedCount;
    private Long score;
}
