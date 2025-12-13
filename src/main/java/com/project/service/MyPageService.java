package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.MyPageResponse;
import com.project.entity.UserEntity;
import com.project.repository.MyPageRepository;
import com.project.repository.RankingDayRepository;
import com.project.repository.UserRepository;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

  private final UserRepository userRepository;
  private final MyPageRepository myPageRepository;
  private final RankingDayRepository rankingDayRepository;

  public MyPageResponse getMyPageData(Long userId, int year, int month, String dateStr) {
    // 1. 유저 조회
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 2. 기준 날짜 설정 & 유효성 검증
    LocalDate targetDate = determineTargetDate(year, month, dateStr);

    // 3. 잔디: 월간 데이터 조회
    List<MyPageResponse.Grass> grassList = myPageRepository.findGrassList(userId, year, month);

    // 4. 상세: 일간 문제 목록 조회 (푼 시간 정렬)
    List<MyPageResponse.Problem> problemList =
        myPageRepository.findSolvedProblemsByDate(userId, targetDate);

    // 5. 통계 계산
    int solvedCount = problemList.size();
    int dailyScore = problemList.stream().mapToInt(MyPageResponse.Problem::getTierLevel).sum();
    int maxDifficulty =
        problemList.stream().mapToInt(MyPageResponse.Problem::getTierLevel).max().orElse(0);

    // 6. 상세: 일간 랭킹 계산 (Native Query)
    long dailyRank = 0;
    if (solvedCount > 0) {
      dailyRank =
          rankingDayRepository.calculateDailyRank(
              dailyScore, targetDate.atStartOfDay(), targetDate.atTime(23, 59, 59));
    }

    // 7. 전체 랭킹 및 점수
    int totalSolvedCount = user.getTotalSolvedCount() != null ? user.getTotalSolvedCount() : 0;

    // 8. DTO 조립 및 변환 (Record 사용)
    DailyStats stats =
        new DailyStats(dailyScore, (int) dailyRank, solvedCount, maxDifficulty, problemList);

    return buildResponse(user, totalSolvedCount, targetDate, stats, grassList);
  }

  /**
   * 날짜 결정 로직. 1. dateStr 있음 -> 해당 날짜 2. dateStr 없음 & 이번 달 조회 -> 오늘 날짜 3. dateStr 없음 & 다른 달 조회 -> 해당
   * 월 1일
   */
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

  /**
   * 내부 데이터 전달용 Record (Java 16+). 불필요한 생성자/Getter 코드를 제거하여 Checkstyle '파라미터 개수' 경고를 회피하고 가독성을 높임.
   */
  private record DailyStats(
      int dailyScore,
      int dailyRank,
      int solvedCount,
      int maxDifficulty,
      List<MyPageResponse.Problem> problems) {
  }

  private MyPageResponse buildResponse(
      UserEntity user,
      int totalScore,
      LocalDate date,
      DailyStats stats,
      List<MyPageResponse.Grass> grassList) {

    MyPageResponse.Profile profile =
        MyPageResponse.Profile.builder()
            .username(user.getUsername())
            .baekjoonId(user.getBaekjoonId())
            .tierLevel(user.getBojTier())
            .totalScore((long) totalScore)
            .build();

    MyPageResponse.SelectedDateDetail detail =
        MyPageResponse.SelectedDateDetail.builder()
            .date(date.toString())
            .dailyScore(stats.dailyScore()) // Record는 .get() 대신 필드명() 사용
            .dailyRank(stats.dailyRank())
            .solvedCount(stats.solvedCount())
            .maxDifficulty(stats.maxDifficulty())
            .problems(stats.problems())
            .build();

    return MyPageResponse.builder()
        .profile(profile)
        .grass(grassList)
        .selectedDateDetail(detail)
        .build();
  }
}
