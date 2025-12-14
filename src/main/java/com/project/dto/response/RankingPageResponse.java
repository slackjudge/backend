package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RankingPageResponse {

    private boolean hasNext;
    private List<RankingRowResponse> rows;
}
