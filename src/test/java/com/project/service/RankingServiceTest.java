package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.repository.RankingQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
    @Mock
    private RankingQueryRepository rankingQueryRepository;

    @InjectMocks
    private RankingService rankingService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
                Instant.parse("2025-12-12T15:48:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        rankingService = new RankingService(rankingQueryRepository, fixedClock);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("랭킹 조회 시 rank 계산 및 hasNext 플래그 올바르게 설정 확인")
    void getRanking_calculatesRankAndHasNext() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2025, 12, 11, 14, 30);

        /** 조회용 데이터
         *  userID, name, tier, totalScore, solvedCount, baekjoonId, team
         *  userA : 100 -> 120점 / userB : 80 -> 80 동일
         */
        List<RankingRowResponse> currentRows = List.of(
                new RankingRowResponse(1L, "userA", 2, 100, 2, "gr2146", "BACKEND_FACE"),
                new RankingRowResponse(2L, "userB", 9, 80, 2, "q1w2e3r4", "BACKEND_NON_FACE")
        );
        List<RankingRowResponse> prevRows = List.of(
                new RankingRowResponse(1L, "userA", 2, 120, 3, "gr2146", "BACKEND_FACE"),
                new RankingRowResponse(2L, "userB", 9, 80, 2, "q1w2e3r4", "BACKEND_NON_FACE")
        );

        when(rankingQueryRepository.getRankingRows(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).thenReturn(currentRows, prevRows);

        // when
        RankingPageResponse response = rankingService.getRanking("day", baseTime, "ALL", 1, 20);

        // then
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getRows()).hasSize(2);
        assertThat(response.getRows().get(0).getRank()).isEqualTo(1);
        assertThat(response.getRows().get(1).getRank()).isEqualTo(2);
    }


    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("현재 랭킹과 직전 랭킹을 비교하여 diff 계산")
    void getRanking_calculatesDiff() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2025, 12, 11, 14, 30);

        /** 조회용 데이터
         *  userID, name, tier, totalScore, solvedCount, baekjoonId, team
         */

        // 현재: userA  1등, userB 2등
        List<RankingRowResponse> currentRows = List.of(
                new RankingRowResponse(1L, "userA", 2, 140, 3, "gr2146", "BACKEND_FACE"),
                new RankingRowResponse(2L, "userB", 9, 120, 3, "q1w2e3r4", "BACKEND_NON_FACE")
        );

        // 직전 : userB 1등, userA 2등
        List<RankingRowResponse> prevRows = List.of(
                new RankingRowResponse(2L, "userB", 9, 100, 2, "q1w2e3r4", "BACKEND_NON_FACE"),
                new RankingRowResponse(1L, "userA", 2, 80, 2, "gr2146", "BACKEND_FACE")
        );

        // 조회 -> 랭킹 -> diff 계산
        when(rankingQueryRepository.getRankingRows(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).thenReturn(currentRows, prevRows);

        // when
        RankingPageResponse response = rankingService.getRanking("day", baseTime, "ALL", 1, 20);

        // then
        RankingRowResponse userA = response.getRows().stream()
                .filter(r -> r.getUserId().equals(1L))
                .findFirst()
                .orElseThrow();

        RankingRowResponse userB = response.getRows().stream()
                .filter(r -> r.getUserId().equals(2L))
                .findFirst()
                .orElseThrow();

        // A는 2등 → 1등 (diff = +1), B는 1등 → 2등 (diff = -1) 라는 식으로 나올 것
        assertThat(userA.getDiff()).isEqualTo(1);
        assertThat(userB.getDiff()).isEqualTo(-1);
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

        /** 조회용 데이터
         *  userID, name, tier, totalScore, solvedCount, baekjoonId, team
         */
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
                isNull()
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
    @Test
    @DisplayName("getRanking - 허용되지 않은 period면 BusinessException(INVALID_RANKING_PERIOD)")
    void getRanking_invalidPeriod() {
        assertThatThrownBy(() -> rankingService.getRanking(
                "year",
                LocalDateTime.of(2025, 12, 11, 14, 30),
                "ALL",
                1,
                20
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RANKING_PERIOD);

        verifyNoInteractions(rankingQueryRepository);
    }

    @Test
    @DisplayName("getRanking - 존재하지 않는 group이면 BusinessException(INVALID_RANKING_GROUP)")
    void getRanking_invalidGroup() {
        assertThatThrownBy(() -> rankingService.getRanking(
                "day",
                LocalDateTime.of(2025, 12, 11, 14, 30),
                "NOT_EXIST",
                1,
                20
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RANKING_GROUP);

        verifyNoInteractions(rankingQueryRepository);
    }

    @Test
    @DisplayName("getRanking - page < 1 또는 size < 1이면 BusinessException(INVALID_RANKING_PAGINATION)")
    void getRanking_invalidPagination() {
        assertThatThrownBy(() -> rankingService.getRanking(
                "day",
                LocalDateTime.of(2025, 12, 11, 14, 30),
                "ALL",
                0,
                20
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RANKING_PAGINATION);

        verifyNoInteractions(rankingQueryRepository);
    }

    @Test
    @DisplayName("getRanking - 현재 구간 랭킹 조회 결과가 비면 정상처리 후 빈 결과 반환")
    void getRanking_noCurrentData() {
        when(rankingQueryRepository.getRankingRows(any(), any(), isNull()))
                .thenReturn(List.of()); // currentAll empty

        RankingPageResponse res = rankingService.getRanking(
                "day", LocalDateTime.of(2025, 12, 11, 14, 30), "ALL", 1, 20
        );

        assertThat(res.isHasNext()).isFalse();
        assertThat(res.getRows()).isEmpty();

        // currentAll 조회 1번에서 바로 터지므로, 호출은 1회까지만 기대 가능
        verify(rankingQueryRepository, times(1)).getRankingRows(any(), any(), isNull());
    }

    @Test
    @DisplayName("getRanking - 요청 page가 범위를 벗어나면 빈 결과 반환으로 변경")
    void getRanking_pageOutOfRange() {
        // currentAll은 1개만 존재
        List<RankingRowResponse> currentRows = List.of(
                new RankingRowResponse(1L, "userA", 2, 100, 2, "bojA", "BACKEND_FACE")
        );
        List<RankingRowResponse> prevRows = List.of(
                new RankingRowResponse(1L, "userA", 2, 90, 1, "bojA", "BACKEND_FACE")
        );

        when(rankingQueryRepository.getRankingRows(any(), any(), isNull()))
                .thenReturn(currentRows, prevRows);

        // page=2, size=20 -> fromIndex=20 >= currentAll.size(1) -> 빈 결과 반환

        RankingPageResponse res = rankingService.getRanking("day", LocalDateTime.of(2025, 12, 11, 14,30), "All", 2, 20);

        assertThat(res.isHasNext()).isFalse();
        assertThat(res.getRows()).isEmpty();

        // current/prev 조회는 둘 다 수행된 다음 슬라이싱에서 터짐
        verify(rankingQueryRepository, times(2)).getRankingRows(any(), any(), isNull());
    }

}
