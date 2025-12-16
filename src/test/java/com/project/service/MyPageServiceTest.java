package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.entity.UserEntity;
import com.project.mapper.MyPageMapper;
import com.project.repository.MyPageRepository;
import com.project.repository.RankingDayRepository;
import com.project.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private RankingDayRepository rankingDayRepository;

    @Mock
    private MyPageMapper myPageMapper;

    @Test
    @DisplayName("성공: 유저가 존재하고 문제를 풀었을 때, 통계(합계, 최대값)와 랭킹이 정상 계산되어야 한다.")
    void getMyPage_Success() {
        // given
        Long userId = 1L;
        String dateStr = "2025-12-05";
        LocalDate targetDate = LocalDate.parse(dateStr);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);

        // 1. Mock 데이터 생성
        UserEntity mockUser = UserEntity.builder().userId(userId).totalSolvedCount(100).build();
        List<ProblemResponse> mockProblems = List.of(
                new ProblemResponse("A", 5, "url1"),
                new ProblemResponse("B", 10, "url2")
        );
        ReflectionTestUtils.setField(mockUser, "createdAt", createdAt);

        // 2. Mock 동작 설정 (Checkstyle 메서드 길이 제한을 위해 헬퍼 메서드로 분리)
        setupSuccessMocks(userId, targetDate, mockUser, mockProblems);

        // when
        MyPageResponse result = myPageService.getMyPage(userId, 2025, 12, dateStr);

        // then
        assertThat(result).isNotNull();

        // [핵심 변경] 4개의 숫자를 DailyStatistics 객체 하나로 묶어서 검증
        // 5 + 10 = 15점, 1등, 2문제, 최대난이도 10
        MyPageMapper.DailyStatistics expectedStats = new MyPageMapper.DailyStatistics(15, 1, 2, 10);

        verify(myPageMapper).toResponse(
                eq(mockUser),
                eq(100),                // totalSolvedCount
                anyList(),              // grassList
                eq(targetDate),         // date
                eq(expectedStats),      // [변경] 통계 객체 검증
                eq(mockProblems)        // problemList
        );

        verify(rankingDayRepository, times(1))
                .calculateDailyRank(eq(15), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("성공: 해당 날짜에 푼 문제가 없을 경우, 점수와 랭킹은 0이어야 하며 랭킹 DB 조회는 건너뛰어야 한다.")
    void getMyPage_NoSolvedProblems() {
        // given
        Long userId = 1L;
        LocalDate targetDate = LocalDate.parse("2025-12-05");
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);

        UserEntity mockUser = UserEntity.builder().userId(userId).createdAt(createdAt).totalSolvedCount(0).build();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(myPageRepository.findSolvedProblemList(userId, targetDate, any(), any())).willReturn(Collections.emptyList());

        ReflectionTestUtils.setField(mockUser, "createdAt", createdAt);

        // [변경] Mapper 파라미터가 6개로 줄어듦 (DailyStatistics 포함)
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(), anyList()))
                .willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2025, 12, "2025-12-05");

        // then
        // [핵심 변경] 모든 통계가 0인 객체 생성
        MyPageMapper.DailyStatistics zeroStats = new MyPageMapper.DailyStatistics(0, 0, 0, 0);

        verify(myPageMapper).toResponse(
                eq(mockUser),
                eq(0),
                anyList(),
                eq(targetDate),
                eq(zeroStats), // [변경] 0으로 채워진 통계 객체 확인
                eq(Collections.emptyList())
        );

        verify(rankingDayRepository, never()).calculateDailyRank(anyInt(), any(), any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 ID 요청 시 예외가 발생해야 한다.")
    void getMyPage_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myPageService.getMyPage(userId, 2025, 12, "2025-12-05"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
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
        given(myPageRepository.findSolvedProblemList(any(), any(), any(), any())).willReturn(Collections.emptyList());

        // [변경] 파라미터 개수 6개로 맞춤
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(), anyList()))
                .willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2020, 1, null);

        // then
        verify(myPageMapper).toResponse(
                any(), anyInt(), anyList(),
                eq(LocalDate.of(2020, 1, 1)), // 기대값
                any(), // DailyStatistics
                anyList()
        );
    }

    @Test
    @DisplayName("검증: 가입 직후 첫 배치 시간대(가입 시간 + 1시간의 정각 ~ 59분)가 제외 시간으로 전달된다.")
    void getMyPage_VerifyIgnoreTimeCalculation() {
        // given
        Long userId = 1L;
        LocalDateTime signUpTime = LocalDateTime.of(2025, 12, 1, 13, 30, 45);

        UserEntity mockUser = UserEntity.builder()
                .userId(userId)
                .createdAt(signUpTime) // [수정] 가입일 설정
                .build();
        ReflectionTestUtils.setField(mockUser, "createdAt", signUpTime);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(myPageRepository.findGrassList(anyLong(), anyInt(), anyInt(), any(), any()))
                .willReturn(Collections.emptyList());
        given(myPageRepository.findSolvedProblemList(any(), any(), any(), any())).willReturn(Collections.emptyList());
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(), anyList()))
                .willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2025, 12, "2025-12-05");

        // then
        // 예상: 13:30 가입 -> 14:00:00 ~ 14:59:59 제외
        LocalDateTime expectedStart = LocalDateTime.of(2025, 12, 1, 14, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2025, 12, 1, 14, 59, 59,999_999_999);

        verify(myPageRepository).findGrassList(
                eq(userId),
                eq(2025),
                eq(12),
                eq(expectedStart),
                eq(expectedEnd)
        );
    }

    // [Checkstyle 해결] 메서드 길이를 줄이기 위한 헬퍼 메서드
    private void setupSuccessMocks(Long userId, LocalDate targetDate,
            UserEntity mockUser, List<ProblemResponse> mockProblems) {
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(myPageRepository.findGrassList(anyLong(), anyInt(), anyInt(), any(), any()))
                .willReturn(Collections.emptyList());
        given(myPageRepository.findSolvedProblemList(userId, targetDate, any(), any()))
                .willReturn(mockProblems);
        given(rankingDayRepository.calculateDailyRank(anyInt(), any(), any()))
                .willReturn(1L);

        // [변경] Mapper Mock 설정 (파라미터 6개)
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), any(MyPageMapper.DailyStatistics.class), anyList()))
                .willReturn(mock(MyPageResponse.class));
    }
}