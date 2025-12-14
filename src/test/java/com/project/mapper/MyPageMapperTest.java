package com.project.mapper;

import com.project.dto.response.GrassResponse;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.entity.UserEntity;
import com.project.mapper.MyPageMapper.DailyStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyPageMapperTest {

    private final MyPageMapper myPageMapper = new MyPageMapper();

    @Test
    @DisplayName("UserEntity와 통계 데이터를 받아 MyPageResponse로 정확히 변환해야 한다.")
    void toResponse_Success() {
        // given
        UserEntity user = createUser();
        DailyStatistics dailyStats = createDailyStats();
        List<GrassResponse> grassList = createGrassList();
        List<ProblemResponse> problemList = createProblemList();
        LocalDate date = LocalDate.of(2025, 12, 15);
        int totalScore = 5000;

        // when
        MyPageResponse response = myPageMapper.toResponse(
                user, totalScore, grassList, date, dailyStats, problemList
        );

        // then
        assertThat(response).isNotNull();

        // 1. 프로필 검증
        assertThat(response.profile().username()).isEqualTo("테스트유저");
        assertThat(response.profile().baekjoonId()).isEqualTo("boj_user");
        assertThat(response.profile().tierLevel()).isEqualTo(15);
        assertThat(response.profile().totalScore()).isEqualTo(5000);

        // 2. 상세 정보 검증
        assertThat(response.selectedDateDetail().date()).isEqualTo(date.toString());
        assertThat(response.selectedDateDetail().dailyScore()).isEqualTo(100);
        assertThat(response.selectedDateDetail().dailyRank()).isEqualTo(1);
        assertThat(response.selectedDateDetail().solvedCount()).isEqualTo(3);
        assertThat(response.selectedDateDetail().maxDifficulty()).isEqualTo(15);

        // 3. 문제 및 잔디 목록 검증
        assertThat(response.selectedDateDetail().problems()).hasSize(2);
        assertThat(response.selectedDateDetail().problems().get(0).title()).isEqualTo("문제1");
        assertThat(response.grass()).hasSize(2);
    }

    // --- Helper Methods to Reduce Method Length ---

    private UserEntity createUser() {
        return UserEntity.builder()
                .username("테스트유저")
                .baekjoonId("boj_user")
                .bojTier(15)
                .build();
    }

    private DailyStatistics createDailyStats() {
        return new DailyStatistics(100, 1, 3, 15);
    }

    private List<GrassResponse> createGrassList() {
        return List.of(
                new GrassResponse("2025-12-14", 5),
                new GrassResponse("2025-12-15", 3)
        );
    }

    private List<ProblemResponse> createProblemList() {
        return List.of(
                new ProblemResponse("문제1", 5, "url1"),
                new ProblemResponse("문제2", 10, "url2")
        );
    }
}