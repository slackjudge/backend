package com.project.mapper;

import com.project.dto.response.GrassResponse;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class MyPageMapperTest {
    private final MyPageMapper myPageMapper = new MyPageMapper();

    @Test
    @DisplayName("UserEntity와 통계 데이터를 받아 MyPageResponse로 정확히 반환")
    void toResponse_Success() {
        // given
        String username = "테스트유저";
        String baekjoonId = "boj_user";
        int bojTier = 15;
        UserEntity user = UserEntity.builder()
                .username(username)
                .baekjoonId(baekjoonId)
                .bojTier(bojTier)
                .build();

        // 2. 통계 데이터 준비
        int totalScore = 5000;
        LocalDate date = LocalDate.of(2025, 12, 15);

        MyPageMapper.DailyStatistics dailyStats = new MyPageMapper.DailyStatistics(
                100, // dailyScore
                1,   // dailyRank
                3,   // solvedCount
                15   // maxDifficulty
        );

        // 3. 리스트 데이터 준비
        List<GrassResponse> grassList = List.of(
                new GrassResponse("2025-12-14", 5),
                new GrassResponse("2025-12-15", 3)
        );

        List<ProblemResponse> problemList = List.of(
                new ProblemResponse("문제1", 5, "url1"),
                new ProblemResponse("문제2", 10, "url2")
        );

        // when
        MyPageResponse response = myPageMapper.toResponse(
                user,
                totalScore,
                grassList,
                date,
                dailyStats,
                problemList
        );

        // then
        assertThat(response).isNotNull();

        // 프로필
        assertThat(response.profile().username()).isEqualTo(username);
        assertThat(response.profile().baekjoonId()).isEqualTo(baekjoonId);
        assertThat(response.profile().tierLevel()).isEqualTo(bojTier); // DTO 필드명 확인 (tierLevel)
        assertThat(response.profile().totalScore()).isEqualTo(totalScore);

        // 2. 상세 정보 검증 (Detail)
        assertThat(response.selectedDateDetail().date()).isEqualTo(date.toString());
        assertThat(response.selectedDateDetail().dailyScore()).isEqualTo(dailyStats.dailyScore());
        assertThat(response.selectedDateDetail().dailyRank()).isEqualTo(dailyStats.dailyRank());
        assertThat(response.selectedDateDetail().solvedCount()).isEqualTo(dailyStats.solvedCount());
        assertThat(response.selectedDateDetail().maxDifficulty()).isEqualTo(dailyStats.maxDifficulty());

        // 문제 목록 검증
        assertThat(response.selectedDateDetail().problems()).hasSize(2);
        assertThat(response.selectedDateDetail().problems().get(0).title()).isEqualTo("문제1");

        // 3. 잔디 검증
        assertThat(response.grass()).hasSize(2);
        assertThat(response.grass().get(0).date()).isEqualTo("2025-12-14");
    }

}