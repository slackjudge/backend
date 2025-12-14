package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.GrassResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
        int year = 2025;
        int month = 12;
        String dateStr = "2025-12-05";
        LocalDate targetDate = LocalDate.parse(dateStr);

        // 1. Mock User
        UserEntity mockUser = UserEntity.builder()
                .userId(userId)
                .username("testUser")
                .totalSolvedCount(100)
                .build();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // 2. Mock Grass List (빈 리스트여도 상관없음)
        given(myPageRepository.findGrassList(userId, year, month)).willReturn(Collections.emptyList());

        // 3. Mock Problem List (통계 계산 검증용 핵심 데이터)
        // 난이도 5, 난이도 10인 문제 2개를 풀었다고 가정
        List<ProblemResponse> mockProblems = List.of(
                new ProblemResponse("A", 5, "url1"),  // tierLevel: 5
                new ProblemResponse("B", 10, "url2")  // tierLevel: 10
        );
        given(myPageRepository.findSolvedProblemList(userId, targetDate)).willReturn(mockProblems);

        // 4. Mock Ranking (문제를 풀었으므로 랭킹 조회 호출됨)
        given(rankingDayRepository.calculateDailyRank(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(1L); // 1등이라고 가정

        // 5. Mock Mapper (결과 반환용)
        MyPageResponse mockResponse = mock(MyPageResponse.class);
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), anyInt(), anyInt(), anyInt(), anyInt(), anyList()))
                .willReturn(mockResponse);

        // when
        MyPageResponse result = myPageService.getMyPage(userId, year, month, dateStr);

        // then
        assertThat(result).isNotNull();

        // [핵심 검증] Mapper에게 전달된 파라미터가 비즈니스 로직대로 계산되었는지 확인
        verify(myPageMapper).toResponse(
                eq(mockUser),
                eq(100),                // totalSolvedCount
                anyList(),              // grassList
                eq(targetDate),         // date
                eq(15),                 // dailyScore (5 + 10 = 15) -> 계산 로직 검증
                eq(1),                  // dailyRank (Mock 반환값)
                eq(2),                  // solvedCount (List size)
                eq(10),                 // maxDifficulty (Max 10) -> 계산 로직 검증
                eq(mockProblems)        // problemList
        );

        // 랭킹 리포지토리가 호출되었는지 확인
        verify(rankingDayRepository, times(1))
                .calculateDailyRank(eq(15), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("성공: 해당 날짜에 푼 문제가 없을 경우, 점수와 랭킹은 0이어야 하며 랭킹 DB 조회는 건너뛰어야 한다.")
    void getMyPage_NoSolvedProblems() {
        // given
        Long userId = 1L;
        String dateStr = "2025-12-05";
        LocalDate targetDate = LocalDate.parse(dateStr);

        UserEntity mockUser = UserEntity.builder().userId(userId).totalSolvedCount(0).build();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(myPageRepository.findSolvedProblemList(userId, targetDate)).willReturn(Collections.emptyList()); // 빈 리스트

        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), anyInt(), anyInt(), anyInt(), anyInt(), anyList()))
                .willReturn(mock(MyPageResponse.class));

        // when
        myPageService.getMyPage(userId, 2025, 12, dateStr);

        // then
        // [핵심 검증] 0으로 계산되어 Mapper에 전달되었는지 확인
        verify(myPageMapper).toResponse(
                eq(mockUser),
                eq(0),
                anyList(),
                eq(targetDate),
                eq(0),  // dailyScore
                eq(0),  // dailyRank
                eq(0),  // solvedCount
                eq(0),  // maxDifficulty
                eq(Collections.emptyList())
        );

        // [중요] 문제가 없으면 랭킹 조회 쿼리가 실행되지 않아야 함 (성능 최적화 확인)
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
        int pastYear = 2020; // 과거
        int pastMonth = 1;

        UserEntity mockUser = UserEntity.builder().userId(userId).build();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(myPageRepository.findSolvedProblemList(any(), any())).willReturn(Collections.emptyList());
        given(myPageMapper.toResponse(any(), anyInt(), anyList(), any(), anyInt(), anyInt(), anyInt(), anyInt(), anyList()))
                .willReturn(mock(MyPageResponse.class));

        // when: 날짜 문자열 없이(null) 호출
        myPageService.getMyPage(userId, pastYear, pastMonth, null);

        // then: Mapper에 넘어간 날짜가 "2020-01-01"인지 확인
        verify(myPageMapper).toResponse(
                any(), anyInt(), anyList(),
                eq(LocalDate.of(2020, 1, 1)), // 기대값
                anyInt(), anyInt(), anyInt(), anyInt(), anyList()
        );
    }
}