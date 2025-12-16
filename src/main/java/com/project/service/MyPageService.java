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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final MyPageRepository myPageRepository;
    private final RankingDayRepository rankingDayRepository;
    private final MyPageMapper myPageMapper; // Mapper 주입


    public MyPageResponse getMyPage(Long userId, int year, int month, String dateStr) {

        // 1. 유저 조회
        UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 제외할 시간대 계산 (가입 직후 첫 배치 시간)
        // 가입: 13:30 -> 제외 범위 : 14:00:00 ~ 14:59:59 -> 15:00
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime ignoreStart = createdAt.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime ignoreEnd = ignoreStart.withMinute(59).withSecond(59).withNano(999_999_999);

        // 2. 리포지토리에 제외할 시간 범위 (ignoreStart, ignoreEnd)
        // Repository에서 solvedTime.notBetween(ignoreStart, ignoreEnd) 처리를 수행
        List<GrassResponse> grassList = myPageRepository.findGrassList(
                userId, year, month, ignoreStart, ignoreEnd
        );

        // 상세 조회 할 날짜 결정
        LocalDate targetDate = determineTargetDate(year, month, dateStr);

        // 상세: 일간 문제 목록 조회 (푼 시간 정렬)
        List<ProblemResponse> problemList =
                myPageRepository.findSolvedProblemList(userId, targetDate, ignoreStart, ignoreEnd);

        // 5. 통계 계산
        MyPageMapper.DailyStatistics dailyStats = calculateDailyStatistics(problemList, targetDate);

        // 7. 전체 랭킹 및 점수
        int totalSolvedCount = user.getTotalSolvedCount() != null ? user.getTotalSolvedCount() : 0;

        return myPageMapper.toResponse(
                user,
                totalSolvedCount,
                grassList,
                targetDate,
                dailyStats,
                problemList
        );
    }
    private MyPageMapper.DailyStatistics calculateDailyStatistics(List<ProblemResponse> problemList, LocalDate targetDate) {
        int solvedCount = problemList.size();
        int dailyScore = problemList.stream().mapToInt(ProblemResponse::tierLevel).sum();
        int maxDifficulty = problemList.stream().mapToInt(ProblemResponse::tierLevel).max().orElse(0);

        int dailyRank = 0;
        if (solvedCount > 0) {
            long rankResult = rankingDayRepository.calculateDailyRank(
                    dailyScore,
                    targetDate.atStartOfDay(),
                    targetDate.plusDays(1).atStartOfDay()
            );
            dailyRank = (int) rankResult;
        }

        return new MyPageMapper.DailyStatistics(dailyScore, dailyRank, solvedCount, maxDifficulty);
    }

    private LocalDate determineTargetDate(int year, int month, String dateStr) {
        try {
            if (dateStr != null && !dateStr.isBlank()) {
                return LocalDate.parse(dateStr);
            }
            LocalDate today = LocalDate.now();
            if (year == today.getYear() && month == today.getMonthValue()) {
                return today;
            }
            return LocalDate.of(year, month, 1);
        } catch (DateTimeException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "날짜 형식이 올바르지 않습니다.");
        }
    }
}
