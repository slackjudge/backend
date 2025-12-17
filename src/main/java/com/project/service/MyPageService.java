package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.RankUtil;
import com.project.dto.response.GrassResponse;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.UserEntity;
import com.project.mapper.MyPageMapper;
import com.project.repository.MyPageRepository;
import com.project.repository.RankingQueryRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
author : 최하영
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final MyPageRepository myPageRepository;
    private final RankingQueryRepository rankingQueryRepository;
    private final MyPageMapper myPageMapper;
    private final Clock clock;


    public MyPageResponse getMyPage(Long userId, int year, int month, String dateStr) {

        UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime validAfter = createdAt.withMinute(0).withSecond(0).withNano(0).plusHours(2);


        // 잔디 데이터 조회 validAfter 이상인 데이터만 조회 
        List<GrassResponse> grassList = myPageRepository.findGrassList(
                userId, year, month, validAfter
        );

        // 상세 조회 할 날짜 결정
        LocalDate targetDate = determineTargetDate(year, month, dateStr);

        // 상세: 일간 문제 목록 조회 
        List<ProblemResponse> problemList =
                myPageRepository.findSolvedProblemList(userId, targetDate, validAfter);

        // 통계 계산
        MyPageMapper.DailyStatistics dailyStats = calculateDailyStatistics(problemList, targetDate, userId);

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
    private MyPageMapper.DailyStatistics calculateDailyStatistics(
            List<ProblemResponse> problemList,
            LocalDate targetDate,
            Long userId) {
        
        int solvedCount = problemList.size();
        int dailyScore = problemList.stream()
                .mapToInt(ProblemResponse::tierLevel)
                .sum();
        int maxDifficulty = problemList.stream()
                .mapToInt(ProblemResponse::tierLevel)
                .max()
                .orElse(0);

        int dailyRank = 0;
        if (solvedCount > 0) {
            LocalDateTime startOfDay = targetDate.atStartOfDay(); 
            LocalDateTime endExclusive;
            
            LocalDate today = LocalDate.now(clock);
            if (targetDate.equals(today)) {
                LocalDateTime now = LocalDateTime.now(clock);
                endExclusive = RankUtil.resolveBaseTime(now);   
            } else {
                // 과거 날짜: 다음날 00:00:00까지
                endExclusive = targetDate.plusDays(1).atStartOfDay(); // 다음날 00:00:00
            }
            
            List<RankingRowResponse> rankingRows = rankingQueryRepository.getRankingRows(
                    startOfDay,
                    endExclusive,
                    null
            );
            
            // 순위 계산 
            calculateRanks(rankingRows);
            
            // 내 userId로 순위 찾기
            dailyRank = rankingRows.stream()
                    .filter(row -> row.getUserId().equals(userId))
                    .findFirst()
                    .map(RankingRowResponse::getRank)
                    .orElse(0); 
        }

        return new MyPageMapper.DailyStatistics(dailyScore, dailyRank, solvedCount, maxDifficulty);
    }

    /**
     * 순위 계산 
     * @param rows
     * @return
     * @throws
     * @throws BusinessException
     * @throws ErrorCode
     * @throws IllegalArgumentException
     * @throws IllegalAccessException   
     */
    private void calculateRanks(List<RankingRowResponse> rows) {
        if (rows.isEmpty()) {
            return;
        }

        rows.get(0).setRank(1);

        for (int i = 1; i < rows.size(); i++) {
            RankingRowResponse prev = rows.get(i - 1);
            RankingRowResponse curr = rows.get(i);

            if (prev.getTotalScore() == curr.getTotalScore()) {
                // 동점자는 같은 순위
                curr.setRank(prev.getRank());
            } else {
                // 점수가 다르면 다음 순위
                curr.setRank(i + 1);
            }
        }
    }

    private LocalDate determineTargetDate(int year, int month, String dateStr) {
        try {
            if (dateStr != null && !dateStr.isBlank()) {
                return LocalDate.parse(dateStr);
            }
            LocalDate today = LocalDate.now(clock);
            if (year == today.getYear() && month == today.getMonthValue()) {
                return today;
            }
            return LocalDate.of(year, month, 1);
        } catch (DateTimeException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "날짜 형식이 올바르지 않습니다.");
        }
    }
}
