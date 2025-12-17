package com.project.dto.response;

import java.util.List;

/*
author : 최하영
*/
public record MyPageResponse(
        MyPageProfileResponse profile,
        List<GrassResponse> grass,
        MyPageDetailResponse selectedDateDetail
) {
}
