package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.dto.response.MyPageResponse;
import com.project.entity.UserEntity;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @InjectMocks
    MyPageService myPageService;

    @Mock UserRepository userRepository;
    @Mock MyPageRepository myPageRepository;
    @Mock RankingDayRepository rankingDayRepository;

    @Test
    @DisplayName("성공: 날짜 파라미터가 없으면 오늘 날짜로 조회한다. (이번 달인 경우) ")
    void getMyPageData_Default_Today(){
        //given
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        UserEntity user = UserEntity.builder().userId(userId).username("최하영").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(myPageRepository.findSolvedProblemsByDate(any(), eq(today))) //오늘 날짜로 호출되어야 함
            .willReturn(Collections.emptyList());

        //when
        // dateStr을 null로 넘김, year/ month는 현재 날짜와 동일하게 설정
        myPageService.getMyPageData(userId, today.getYear(), today.getMonthValue(), null);

        //then
        verify(myPageRepository).findSolvedProblemsByDate(eq(userId), eq(today));
    }

    @Test
    @DisplayName("성공: 날짜 파라미터 없고 다른달일 경우 1일로 조회한다. ")
    void getMyPageData_Default_FirstDay(){
        //given
        Long userId = 1L;
        int futureYear = 2026;
        int futureMonth = 2;
        LocalDate expectedDate = LocalDate.of(futureYear, futureMonth, 1);

        UserEntity user = UserEntity.builder().userId(userId).username("최하영").build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        myPageService.getMyPageData(userId, futureYear, futureMonth, null);

        //then
        verify(myPageRepository).findSolvedProblemsByDate(eq(userId), eq(expectedDate));
    }

    @Test
    @DisplayName("계산 로직: 문제 목록을 기반으로 총점, 최고 난이도 계산")
    void getMyPageData_Calculation(){
        //given
        Long userId = 1L;
        LocalDate targetDate = LocalDate.of(2025, 12, 11);
        UserEntity user = UserEntity.builder().userId(userId).username("최하영").build();
        //문제 2개
        List<MyPageResponse.Problem> problemList = List.of(
                MyPageResponse.Problem.builder().title("A+B").tierLevel(5).build(),
                MyPageResponse.Problem.builder().title("별찍기").tierLevel(10).build()
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(myPageRepository.findSolvedProblemsByDate(any(), any())).willReturn(problemList);
        //랭킹 레포지토리 리턴 -> 일단 아무 값
        given(rankingDayRepository.calculateDailyRank(anyInt(), any(), any())).willReturn(1L);

        //when
        MyPageResponse response = myPageService.getMyPageData(userId, 2025, 12, "2025-12-11");

        //then
        MyPageResponse.SelectedDateDetail detail = response.getSelectedDateDetail();

        assertThat(detail.getSolvedCount()).isEqualTo(2);
        assertThat(detail.getDailyScore()).isEqualTo(15);
        assertThat(detail.getMaxDifficulty()).isEqualTo(10);
    }

    @Test
    @DisplayName("최적화: 푼 문제가 없으면, 랭킹 DB 조회를 하지 않는다. ")
    void getMyPageData_NoProblems_SkipRanking(){
        //given
        Long userId = 1L;
        UserEntity user = UserEntity.builder().userId(userId).username("최하영").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(myPageRepository.findSolvedProblemsByDate(any(), any())).willReturn(Collections.emptyList());

        //when
        myPageService.getMyPageData(userId, 2025, 12, "2025-12-11");

        //then
        //rankingDayRepository.calculateDailyRank()가 호출되지 않는다.
        verify(rankingDayRepository, never()).calculateDailyRank(anyInt(), any(), any());
    }

    @Test
    @DisplayName("예외: 존재하지 않는 유저는 USER_NOT_FOUND 예외 발생")
    void getMyPageData_UserNotFound() {
        // given (없는 ID가 들어왔다고 가정)
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then (비즈니스 예외가 터져야 정상!)
        assertThatThrownBy(() -> myPageService.getMyPageData(99L, 2025, 12, null))
                .isInstanceOf(BusinessException.class);
    }


}