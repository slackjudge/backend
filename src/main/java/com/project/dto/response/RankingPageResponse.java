package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * author : 박준희
 */
@Getter
@AllArgsConstructor
public class RankingPageResponse {

    private boolean hasNext;
    private List<RankingRowResponse> rows;
}
