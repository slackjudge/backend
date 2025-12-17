package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.RankUtil;
import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
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

  /**
   * 해당 시간대의 랭킹과 직전 시간대 랭킹을 비교하여 반환
   * @Param  사용자가 요청한 시간대 (예: 2025.10.12T14:21:29)
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


  private String normalizeAndValidatePeriod(String period) {
    String p = (period == null) ? "day" : period.toLowerCase();
    if (!ALLOWED_PERIOD.contains(p)) {
      throw new BusinessException(ErrorCode.INVALID_RANKING_PERIOD);
    }
    return p;
  }

  private EurekaTeamName parseAndValidateGroup(String group) {
    String g = (group == null) ? "ALL" : group.toUpperCase();
    if ("ALL".equals(g)) return null;

    try {
      return EurekaTeamName.valueOf(g);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.INVALID_RANKING_GROUP);
    }
  }

  private void validatePagination(int page, int size) {
    if (page < 1 || size < 1) {
      throw new BusinessException(ErrorCode.INVALID_RANKING_PAGINATION);
    }
  }


  /**
   *  diff = 1시간 전 rank - 현재 rank (+순위 상승, -순위 하락, 0 변동x)
   */
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


  /**
   * 순위 계산 - 동점자는 같은순위, 이름순 정렬
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
        curr.setRank(prev.getRank());
      } else {
        curr.setRank(i + 1);
      }
    }
  }
}


