package com.project.service;

import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.repository.RankingQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
    @Mock
    private RankingQueryRepository rankingQueryRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getRanking - 현재/이전 랭킹을 비교해 diff와 rank를 계산")
    void getRanking_calculatesDiffAndRank() {
        // given
        String period = "day";
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 11, 14, 30);
        String group = "ALL";
        int page = 1;
        int size = 2;         // 한 페이지에 2명

        // 현재 랭킹 데이터 (이미 정렬됐다고 가정)
        RankingRowResponse currentUser1 =
                new RankingRowResponse(1L, 0, 5, "user1", 100, 10L, "boj1", "BACKEND", 0);
        RankingRowResponse currentUser2 =
                new RankingRowResponse(2L, 0, 4, "user2", 80,  8L, "boj2", "BACKEND", 0);

        List<RankingRowResponse> currentRows = List.of(currentUser1, currentUser2);

        // 이전 랭킹 데이터
        // (user2가 1등, user1이 2등이었다고 가정 -> 순위가 뒤집힘)
        RankingRowResponse prevUser1 =
                new RankingRowResponse(1L, 0, 5, "user1", 80,  8L, "boj1", "BACKEND", 0);
        RankingRowResponse prevUser2 =
                new RankingRowResponse(2L, 0, 4, "user2", 100, 10L, "boj2", "BACKEND", 0);

        List<RankingRowResponse> prevRows = List.of(prevUser2, prevUser1);
        // prevRows는 totalScore 기준으로 정렬된 상태( user2 -> user1 )

        // Repository 호출 시 순서대로 현재 / 이전 랭킹 반환
        when(rankingQueryRepository.getRankingRows(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(currentRows, prevRows);

        // when
        RankingPageResponse result = rankingService.getRanking(period, dateTime, group, page, size);

        // then
        // Repository가 두 번 호출되었는지 확인
        verify(rankingQueryRepository, times(2))
                .getRankingRows(any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyString(),
                        anyInt(),
                        anyInt());

        // hasNext : currentRows.size() == size -> false
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getRows()).hasSize(2);

        RankingRowResponse r1 = result.getRows().get(0);
        RankingRowResponse r2 = result.getRows().get(1);

        // 순위 계산 검증
        assertThat(r1.getUserId()).isEqualTo(1L);
        assertThat(r1.getRank()).isEqualTo(1); // 점수 100으로 1등

        assertThat(r2.getUserId()).isEqualTo(2L);
        assertThat(r2.getRank()).isEqualTo(2); // 점수 80으로 2등

        // diff = 이전 rank - 현재 rank
        // prev에서 user1은 2등, user2는 1등
        // user1: 2 - 1 = +1 (순위 상승 1)
        // user2: 1 - 2 = -1 (순위 하락 1)

        assertThat(r1.getDiff()).isEqualTo(1);
        assertThat(r2.getDiff()).isEqualTo(-1);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getRanking - 이전 랭킹에 없던 유저는 diff=0으로 처리")
    void getRanking_diffZeroWhenNoPrevRank() {
        // given
        String period = "day";
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 11, 14, 30);
        String group = "ALL";
        int page = 1;
        int size = 2;

        RankingRowResponse currentUser1 =
                new RankingRowResponse(1L, 0, 5, "user1", 100, 10L, "boj1", "BACKEND", 0);
        RankingRowResponse currentUser2 =
                new RankingRowResponse(2L, 0, 4, "user2", 80,  8L, "boj2", "BACKEND", 0);

        List<RankingRowResponse> currentRows = List.of(currentUser1, currentUser2);

        // 이전 랭킹엔 user1만 있고, user2는 새로 진입한 케이스
        RankingRowResponse prevUser1 =
                new RankingRowResponse(1L, 0, 5, "user1", 90,  9L, "boj1", "BACKEND", 0);
        List<RankingRowResponse> prevRows = List.of(prevUser1);

        when(rankingQueryRepository.getRankingRows(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(currentRows, prevRows);

        // when
        RankingPageResponse result = rankingService.getRanking(period, dateTime, group, page, size);

        // then
        RankingRowResponse r1 = result.getRows().get(0);
        RankingRowResponse r2 = result.getRows().get(1);

        // user1 : 이전 rank 1, 현재 rank 1 → diff = 0
        assertThat(r1.getDiff()).isEqualTo(0);

        // user2 : 이전 랭킹에 없음 → diff = 0 처리
        assertThat(r2.getDiff()).isEqualTo(0);
    }
}
