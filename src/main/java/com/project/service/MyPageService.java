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

        // 1. 유저 조회
        UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 제외할 시간대 계산 (가입 직후 첫 배치 시간)
        LocalDateTime createdAt = user.getCreatedAt();
        //배치 도는 유효한 시간
        LocalDateTime validAfter = createdAt.withMinute(0).withSecond(0).withNano(0).plusHours(2);


        // 2. 잔디 데이터 조회 (validAfter 이상인 데이터만 조회)
        List<GrassResponse> grassList = myPageRepository.findGrassList(
                userId, year, month, validAfter
        );

        // 상세 조회 할 날짜 결정
        LocalDate targetDate = determineTargetDate(year, month, dateStr);

        // 상세: 일간 문제 목록 조회 (푼 시간 정렬)
        List<ProblemResponse> problemList =
                myPageRepository.findSolvedProblemList(userId, targetDate, validAfter);

        // 5. 통계 계산
        MyPageMapper.DailyStatistics dailyStats = calculateDailyStatistics(problemList, targetDate, userId);

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
            // 1. 시간 범위 계산
            LocalDateTime startOfDay = targetDate.atStartOfDay(); // 00:00:00
            LocalDateTime endExclusive;
            
            LocalDate today = LocalDate.now(clock);
            if (targetDate.equals(today)) {
                // 오늘 날짜: 현재 시간의 정각까지 (1시간 단위 배치 기준)
                LocalDateTime now = LocalDateTime.now(clock);
                endExclusive = RankUtil.resolveBaseTime(now); // 예: 15:00:00
            } else {
                // 과거 날짜: 다음날 00:00:00까지
                endExclusive = targetDate.plusDays(1).atStartOfDay(); // 다음날 00:00:00
            }
            
            // 2. RankingQueryRepository로 랭킹 조회
            // start: 00:00:00 (gt 조건이므로 00:00:00은 제외됨)
            // endExclusive: 오늘은 현재 정각, 과거는 다음날 00:00:00
            List<RankingRowResponse> rankingRows = rankingQueryRepository.getRankingRows(
                    startOfDay,      // gt 조건: 00:00:00 제외
                    endExclusive,    // loe 조건: endExclusive 이하 포함
                    null             // 전체 그룹
            );
            
            // 3. 순위 계산 (RankingService의 calculateRanks 로직)
            calculateRanks(rankingRows);
            
            // 4. 내 userId로 순위 찾기
            dailyRank = rankingRows.stream()
                    .filter(row -> row.getUserId().equals(userId))
                    .findFirst()
                    .map(RankingRowResponse::getRank)
                    .orElse(0); // 랭킹에 없으면 0 (문제를 안 풀었거나 점수가 0)
        }

        return new MyPageMapper.DailyStatistics(dailyScore, dailyRank, solvedCount, maxDifficulty);
    }

    /**
     * 순위 계산 - 동점자는 같은순위, 이름순 정렬
     * RankingService의 calculateRanks 로직과 동일
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
