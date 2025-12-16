package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.UserEntity;
import com.project.mapper.MyPageMapper;
import com.project.repository.MyPageRepository;
import com.project.repository.RankingQueryRepository;
import com.project.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MyPageRepository myPageRepository;

    @Mock
    private RankingQueryRepository rankingQueryRepository;

    @Mock
    private MyPageMapper myPageMapper;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("성공: 유저가 존재하고 문제를 풀었을 때, 통계(합계, 최대값)와 랭킹이 정상 계산되어야 한다.")
    void getMyPage_Success() {
        // given
        Long userId = 1L;
        String dateStr = "2025-12-05";
        LocalDate targetDate = LocalDate.parse(dateStr);
        LocalDateTime createAt = LocalDateTime.of(2025, 1, 1, 13, 0, 0);

        // 1. Mock 데이터 생성
        UserEntity mockUser = UserEntity.builder().userId(userId).totalSolvedCount(100).build();
        List<ProblemResponse> mockProblems = List.of(new ProblemResponse("A", 5, "url1"), new ProblemResponse("B", 10, "url2"));
        ReflectionTestUtils.setField(mockUser, "createdAt", createAt);

        // 2. Mock 동작 설정
        setupSuccessMocks(userId, targetDate, mockUser, mockProblems);

        // when
        MyPageResponse result = myPageService.getMyPage(userId, 2025, 12, dateStr);

        // then
        assertThat(result).isNotNull();

        // 5 + 10 = 15점, 1등, 2문제, 최대난이도 10
        MyPageMapper.DailyStatistics expectedStats = new MyPageMapper.DailyStatistics(15, 1, 2, 10);

        verify(myPageMapper).toResponse(eq(mockUser), eq(100),                // totalSolvedCount
                anyList(),              // grassList
                eq(targetDate),         // date
                eq(expectedStats),      // 통계 객체 검증
                eq(mockProblems)        // problemList
        );

        verify(rankingQueryRepository, times(1)).getRankingRows(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("성공: 해당 날짜에 푼 문제가 없을 경우, 점수와 랭킹은 0이어야 하며 랭킹 DB 조회는 건너뛰어야 한다.")
    void getMyPage_NoSolvedProblems() {
        // given
        Long userId = 1L;
        LocalDate targetDate = LocalDate.parse("2025-12-05");
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);

        UserEntity mockUser = UserEntity.builder().userId(userId).createdAt(createdAt).totalSolvedCount(0).build();
        ReflectionTestUtils.setField(mockUser, "createdAt", createdAt);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // ignoreStart/End 대신 any() 사용
        given(myPageRepository.findSolvedProblemList(eq(userId), eq(targetDate), any())).willReturn(Collections.emptyList());

        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(), anyList())).willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2025, 12, "2025-12-05");

        // then
        MyPageMapper.DailyStatistics zeroStats = new MyPageMapper.DailyStatistics(0, 0, 0, 0);

        verify(myPageMapper).toResponse(eq(mockUser), eq(0), anyList(), eq(targetDate), eq(zeroStats), eq(Collections.emptyList()));

        verify(rankingQueryRepository, never()).getRankingRows(any(), any(), any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 ID 요청 시 예외가 발생해야 한다.")
    void getMyPage_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myPageService.getMyPage(userId, 2025, 12, "2025-12-05")).isInstanceOf(BusinessException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("날짜 로직 검증: dateStr이 null이면 year/month의 1일이나 오늘 날짜가 설정되어야 한다.")
    void getMyPage_DateLogic() {
        // given
        Long userId = 1L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        UserEntity mockUser = UserEntity.builder().userId(userId).createdAt(createdAt).build();
        ReflectionTestUtils.setField(mockUser, "createdAt", createdAt);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(clock.instant()).willReturn(java.time.Instant.now());
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        // ignoreStart, ignoreEnd 대신 any() 사용
        given(myPageRepository.findSolvedProblemList(any(), any(), any())).willReturn(Collections.emptyList());

        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(), anyList())).willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2020, 1, null);

        // then
        verify(myPageMapper).toResponse(any(), anyInt(), anyList(), eq(LocalDate.of(2020, 1, 1)), // 기대값
                any(), anyList());
    }

    @Test
    @DisplayName("검증: 가입 직후 두번째 배치시점인 2시간을 더한 시점을 기준 시간으로 정한다.")
    void getMyPage_VerifyIgnoreTimeCalculation() {
        // given
        Long userId = 1L;
        // 시나리오: 13:58
        LocalDateTime signUpTime = LocalDateTime.of(2025, 12, 1, 13, 58, 45);

        UserEntity mockUser = UserEntity.builder().userId(userId).build();
        ReflectionTestUtils.setField(mockUser, "createdAt", signUpTime);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // Mocking: any()를 사용하여 어떤 시간이 들어오든 에러가 안 나게 설정
        given(myPageRepository.findGrassList(anyLong(), anyInt(), anyInt(), any())).willReturn(Collections.emptyList());
        given(myPageRepository.findSolvedProblemList(any(), any(), any())).willReturn(Collections.emptyList());
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(), anyList())).willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2025, 12, "2025-12-01");

        // then
        // 예상: 13:30 가입 -> 14:00:00.000 ~ 14:59:59.999... 제외
        LocalDateTime expectedStart = LocalDateTime.of(2025, 12, 1, 15, 0, 0);

        // Repository에 15:00가 넘어갔는지 확인
        verify(myPageRepository).findGrassList(
                eq(userId),
                eq(2025),
                eq(12),
                eq(expectedStart)
        );
    }

    private void setupSuccessMocks(Long userId, LocalDate targetDate, UserEntity mockUser, List<ProblemResponse> mockProblems) {
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        given(myPageRepository.findGrassList(anyLong(), anyInt(), anyInt(), any())).willReturn(Collections.emptyList());
        given(myPageRepository.findSolvedProblemList(eq(userId), eq(targetDate), any())).willReturn(mockProblems);

        // RankingQueryRepository mock 설정
        // 내 userId가 포함된 랭킹 리스트 반환 (1등으로 설정)
        RankingRowResponse myRankingRow = new RankingRowResponse(userId, "testUser", 10, 15, 2, "testId", "BACKEND");
        myRankingRow.setRank(1);
        given(rankingQueryRepository.getRankingRows(any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .willReturn(List.of(myRankingRow));

        given(clock.instant()).willReturn(java.time.Instant.now());
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(MyPageMapper.DailyStatistics.class), anyList())).willReturn(mock(MyPageResponse.class));
    }
}