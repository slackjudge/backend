package com.project.repository;

import com.project.config.QueryDslConfig;
import com.project.config.security.JpaAuditingConfig;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
/**
 * author : 박준희
 */
@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class, RankingQueryRepository.class})
@ActiveProfiles("test")
@Sql(
        value = "/sql/insert-ranking-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class RankingQueryRepositoryTest {

    @Autowired
    RankingQueryRepository rankingQueryRepository;

    @Test
    @DisplayName("group = ALL, 2025-12 전체 기간 → 점수 desc + 이름 asc로 정렬")
    void getRankingRows_allGroup_basicOrderAndAggregation() {
        // given
        LocalDateTime start = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2026, 1, 1).atStartOfDay();

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, null);

        // then
        assertThat(rows).hasSize(3);

        RankingRowResponse first = rows.get(0);
        RankingRowResponse second = rows.get(1);
        RankingRowResponse third = rows.get(2);

        // 1위
        assertThat(first.getUserId()).isEqualTo(2L);
        assertThat(first.getName()).isEqualTo("프론트유저");
        assertThat(first.getTotalScore()).isEqualTo(35);
        assertThat(first.getSolvedCount()).isEqualTo(3L);
        assertThat(first.getTeam()).isEqualTo("FRONTEND_FACE");

        // 2위
        assertThat(second.getUserId()).isEqualTo(1L);
        assertThat(second.getName()).isEqualTo("백엔드유저");
        assertThat(second.getTotalScore()).isEqualTo(25);
        assertThat(second.getSolvedCount()).isEqualTo(2L);
        assertThat(second.getTeam()).isEqualTo("BACKEND_FACE");

        // 3위
        assertThat(third.getUserId()).isEqualTo(3L);
        assertThat(third.getName()).isEqualTo("비대면유저");
        assertThat(third.getTotalScore()).isEqualTo(15);
        assertThat(third.getSolvedCount()).isEqualTo(1L);
        assertThat(third.getTeam()).isEqualTo("FRONTEND_NON_FACE");
    }

    @Test
    @DisplayName("group = BACKEND_FACE 인 경우 해당 팀 유저만 집계")
    void getRankingRows_filterByBackendFaceGroup() {
        // given
        LocalDateTime start = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2026, 1, 1).atStartOfDay();
        String group = "BACKEND_FACE";

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, EurekaTeamName.BACKEND_FACE);

        // then
        assertThat(rows).hasSize(1);

        RankingRowResponse backendRow = rows.get(0);
        assertThat(backendRow.getUserId()).isEqualTo(1L);
        assertThat(backendRow.getName()).isEqualTo("백엔드유저");
        assertThat(backendRow.getTeam()).isEqualTo("BACKEND_FACE");
        assertThat(backendRow.getTotalScore()).isEqualTo(25);
        assertThat(backendRow.getSolvedCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[start, endExclusive) 기간 필터가 올바르게 적용되어 endExclusive 시각 이후 데이터는 제외")
    void getRankingRows_periodFilter_exclusiveEnd() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 12, 11, 13, 0);
        LocalDateTime endExclusive = LocalDateTime.of(2025, 12, 11, 14, 0);

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, null);

        // then
        assertThat(rows).hasSize(3);

        RankingRowResponse backendRow = findByName(rows, "백엔드유저");
        RankingRowResponse frontendRow = findByName(rows, "프론트유저");
        RankingRowResponse nonFaceRow = findByName(rows, "비대면유저");

        assertThat(backendRow.getTotalScore()).isEqualTo(25);
        assertThat(backendRow.getSolvedCount()).isEqualTo(2L);

        assertThat(frontendRow.getTotalScore()).isEqualTo(15);
        assertThat(frontendRow.getSolvedCount()).isEqualTo(2L);

        assertThat(nonFaceRow.getTotalScore()).isEqualTo(15);
        assertThat(nonFaceRow.getSolvedCount()).isEqualTo(1L);
    }

    private RankingRowResponse findByName(List<RankingRowResponse> rows, String name) {
        return rows.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("랭킹 결과에서 유저를 찾을 수 없음: " + name));
    }
}
