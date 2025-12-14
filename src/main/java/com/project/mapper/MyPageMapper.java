package com.project.mapper;

import com.project.dto.response.GrassResponse;
import com.project.dto.response.MyPageProfileResponse;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.dto.response.MyPageDetailResponse;
import com.project.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class MyPageMapper {
    public MyPageResponse toResponse(UserEntity user, int totalScore,
            List<GrassResponse> grassList,
            LocalDate date, int dailyScore, int dailyRank, int solvedCount, int maxDifficulty,
            List<ProblemResponse> problemList) {

        // 1. 프로필 Record 생성
        MyPageProfileResponse profile = new MyPageProfileResponse(
                user.getUsername(),
                user.getBaekjoonId(),
                user.getBojTier(),
                (long) totalScore
        );

        // 2. 상세 정보 Record 생성
        MyPageDetailResponse detail = new MyPageDetailResponse(
                date.toString(),
                dailyScore,
                dailyRank,
                solvedCount,
                maxDifficulty,
                problemList
        );

        // 3. 최종 Record 조립 후 반환
        return new MyPageResponse(profile, grassList, detail);
    }
}
