package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * author : 박준희
 */
@Getter
@AllArgsConstructor
public class RankingPageExtendedResponse {
    private boolean hasNext;
    private LocalDateTime updateTime;
    private List<RankingRowExtendedResponse> rows;
}
