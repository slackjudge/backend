package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.RankUtil;
import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
import com.project.repository.BatchMetaRepository;
import com.project.repository.RankingQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

  private static final Set<String> ALLOWED_PERIOD = Set.of("day", "week", "month");

  private final RankingQueryRepository rankingQueryRepository;
  private final Clock clock;
  private final BatchMetaRepository batchMetaRepository;


  /**
   * 배치 테이블 사용에 따른 end_Time 변경
   * version : 1.0.1
   */
  public RankingPageResponse getRankingForBatch(String period, LocalDateTime dateTime, String group, int page, int size) {

    String normalizedPeriod = normalizeAndValidatePeriod(period);
    EurekaTeamName team = parseAndValidateGroup(group);
    validatePagination(page, size);

    // 배치 최신 스냅샷(UTC -> KST 변화 후 절삭)
    LocalDateTime availableEnd = batchMetaRepository.findLastCompletedEndTime()
            .map(RankUtil::utcToKst)
            .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_SNAPSHOT_NOT_READY));

   // 사용자가 요청한 시각(절삭)
    LocalDateTime effective = (dateTime != null) ? dateTime : LocalDateTime.now(clock);
    LocalDateTime reqBaseTime = RankUtil.resolveBaseTime(effective);

    log.info("[INFO] 사용자의 요청 시각 = {}", reqBaseTime);
    log.info("[INFO] 최신 스냅샷 시각 = {}", availableEnd);


    // 요청 기간의 시작과 끝 경계 계산
    LocalDateTime periodStart = RankUtil.getPeriodStart(normalizedPeriod, reqBaseTime);
    LocalDateTime periodEndBoundary = RankUtil.getPeriodEndBoundary(normalizedPeriod, periodStart);


    // 실제 조회 가능한 end는 최근 스냅샷 or 요청 경계 값 중 좀 더 이른 값
    LocalDateTime endInclusive = availableEnd.isBefore(periodEndBoundary)
            ? availableEnd
            : periodEndBoundary;

    // 현재/과거 판단
    boolean isCurrentPeriod = RankUtil.getPeriodStart(normalizedPeriod, availableEnd).equals(periodStart);

    // === 과거 구간 : 1번 쿼리, 순위 변동 계산 x ===
    if(!isCurrentPeriod) {
      List<RankingRowResponse> rows = rankingQueryRepository.getRankingRows(periodStart, endInclusive, team);

      if(rows.isEmpty()) {
        return new RankingPageResponse(false, List.of());
      }

      calculateRanks(rows);
      rows.forEach(r -> r.setDiff(0));

      return paginate(rows, page, size);
    }


    // === 현재 구간 : 2번 쿼리, 순위 변동 계산 o ===
    LocalDateTime currentEndInclusive = endInclusive;
    LocalDateTime prevEndInclusive = currentEndInclusive.minusHours(1);


    // 현재 구간 집계 1번 쿼리
    List<RankingRowResponse> currentAll = rankingQueryRepository.getRankingRows(periodStart, currentEndInclusive, team);

    if (currentAll.isEmpty()) {
      return new RankingPageResponse(false, List.of());
    }

    // 1시간 전 구간 집계 2번 쿼리
    List<RankingRowResponse> prevAll = rankingQueryRepository.getRankingRows(periodStart, prevEndInclusive, team);

    calculateRanks(currentAll);
    calculateRanks(prevAll);
    calculateDiff(currentAll, prevAll);

    return paginate(currentAll, page, size);
  }


  /**
   * 해당 시간대의 랭킹과 직전 시간대 랭킹을 비교하여 반환
   * @Param  사용자가 요청한 시간대 (예: 2025.10.12T14:21:29)
   * 버그 시 해당 함수로 롤백
   */
  public RankingPageResponse getRanking(String period, LocalDateTime dateTime, String group, int page, int size) {

    String normalizedPeriod = normalizeAndValidatePeriod(period);
    EurekaTeamName team = parseAndValidateGroup(group);
    validatePagination(page, size);

    LocalDateTime effective = (dateTime != null) ? dateTime : LocalDateTime.now(clock);
    LocalDateTime baseTime = RankUtil.resolveBaseTime(effective);
    LocalDateTime now = LocalDateTime.now(clock);

    LocalDateTime periodStart = RankUtil.getPeriodStart(normalizedPeriod, baseTime);

    LocalDateTime currentEndInclusive = RankUtil.getPeriodEndInclusive(normalizedPeriod, baseTime, now);
    LocalDateTime prevEndInclusive = currentEndInclusive.minusHours(1);
    List<RankingRowResponse> currentAll = rankingQueryRepository.getRankingRows(periodStart, currentEndInclusive, team);

    if (currentAll.isEmpty()) {
       return new RankingPageResponse(false, List.of());
    }

    List<RankingRowResponse> prevAll = rankingQueryRepository.getRankingRows(periodStart, prevEndInclusive, team);

    calculateRanks(currentAll);
    calculateRanks(prevAll);
    calculateDiff(currentAll, prevAll);

    int fromIndex = Math.max(0, (page - 1) * size);
    if (fromIndex >= currentAll.size()) {
      return new RankingPageResponse(false, List.of());
    }

    int toIndex = Math.min(fromIndex + size, currentAll.size());
    List<RankingRowResponse> pageRows = currentAll.subList(fromIndex, toIndex);
    boolean hasNext = toIndex < currentAll.size();

    return new RankingPageResponse(hasNext, pageRows);
  }


  // ===== 계산&유효성 검사 =====

  // 페이징 처리
  private RankingPageResponse paginate(List<RankingRowResponse> rows, int page, int size) {
    int fromIndex = Math.max(0, (page - 1) * size);
    if (fromIndex >= rows.size()) {
      return new RankingPageResponse(false, List.of());
    }

    int toIndex = Math.min(fromIndex + size, rows.size());
    List<RankingRowResponse> pageRows = rows.subList(fromIndex, toIndex);
    boolean hasNext = toIndex < rows.size();
    return new RankingPageResponse(hasNext, rows.subList(fromIndex, toIndex));
  }


  // 기간 유효성 검증
  private String normalizeAndValidatePeriod(String period) {
    String p = (period == null) ? "day" : period.toLowerCase();
    if (!ALLOWED_PERIOD.contains(p)) {
      throw new BusinessException(ErrorCode.INVALID_RANKING_PERIOD);
    }
    return p;
  }


  // 그룹 유효성 검증
  private EurekaTeamName parseAndValidateGroup(String group) {
    String g = (group == null) ? "ALL" : group.toUpperCase();
    if ("ALL".equals(g)) return null;

    try {
      return EurekaTeamName.valueOf(g);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.INVALID_RANKING_GROUP);
    }
  }


  // 페이징 유효성 검증
  private void validatePagination(int page, int size) {
    if (page < 1 || size < 1) {
      throw new BusinessException(ErrorCode.INVALID_RANKING_PAGINATION);
    }
  }


  // 순위 변동 비교(상승 +, 하락 -, 변동없음 -)
  private void calculateDiff(List<RankingRowResponse> current,
                             List<RankingRowResponse> previous) {

    Map<Long, RankingRowResponse> prevByUserId = previous.stream()
            .collect(Collectors.toMap(
                    RankingRowResponse::getUserId,
                    r -> r
            ));

    for (RankingRowResponse curRow : current) {
      RankingRowResponse prevRow = prevByUserId.get(curRow.getUserId());

      if (prevRow == null || prevRow.getRank() == 0) {
        curRow.setDiff(0);
      } else {
          int diff = prevRow.getRank() - curRow.getRank();
          curRow.setDiff(diff);
      }
    }
  }


//순위 계산, 동점자는 같은순위, 이름순 정렬
  private void calculateRanks(List<RankingRowResponse> rows) {

    if (rows.isEmpty()) {
      return;
    }

    rows.get(0).setRank(1);

    for (int i = 1; i < rows.size(); i++) {
      RankingRowResponse prev = rows.get(i - 1);
      RankingRowResponse curr = rows.get(i);

      if (prev.getTotalScore() == curr.getTotalScore()) {
        curr.setRank(prev.getRank());
      } else {
        curr.setRank(i + 1);
      }
    }
  }
}


