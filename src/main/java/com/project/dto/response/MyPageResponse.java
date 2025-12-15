package com.project.dto.response;

import java.util.List;

//MyPageResponse.record
public record MyPageResponse(
        MyPageProfileResponse profile,
        List<GrassResponse> grass,
        MyPageDetailResponse selectedDateDetail
) {
}
