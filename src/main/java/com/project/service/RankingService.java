package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.RankUtil;
import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
import com.project.repository.RankingQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RankingService {

  private static final Set<String> ALLOWED_PERIOD = Set.of("day", "week", "month");

  private final RankingQueryRepository rankingQueryRepository;
  private final Clock clock;

  /**
   * 해당 시간대의 랭킹과 직전 시간대 랭킹을 비교하여 반환
   */
  public RankingPageResponse getRanking(String period, LocalDateTime dateTime, String group, int page, int size) {

    String normalizedPeriod = normalizeAndValidatePeriod(period);
    EurekaTeamName team = parseAndValidateGroup(group);
    validatePagination(page, size);

    // 도커 컨테이너 타임존 한국 시간으로 변경
    LocalDateTime effective = (dateTime != null) ? dateTime : LocalDateTime.now(clock);

    // 기준 시각 정규화 (null -> now, 분/초/나노 0)
    LocalDateTime baseTime = RankUtil.resolveBaseTime(effective);

    // 집계 시작 시각 (주의 월요일, 달의 첫째 날..) 2025-12-01T00:00:00
    LocalDateTime periodStart = RankUtil.getPeriodStart(normalizedPeriod, baseTime);

    // [start, endExclusive) 구간 설정
    LocalDateTime currentEndExclusive = RankUtil.getPeriodEndExclusive(baseTime);              // ex) 15:00
    LocalDateTime prevEndExclusive    = RankUtil.getPeriodEndExclusive(baseTime.minusHours(1)); // ex) 14:00


    // 현재 구간 랭킹 조회
    List<RankingRowResponse> currentAll = rankingQueryRepository.getRankingRows(periodStart, currentEndExclusive, team);

    if (currentAll.isEmpty()) {
      throw new BusinessException(ErrorCode.RANKING_NOT_FOUND, "선택한 기간/그룹에 대한 데이터가 존재하지 않습니다.");
    }

    // 이전 구간 랭킹 조회 (직전 한 시간 전까지)
    List<RankingRowResponse> prevAll = rankingQueryRepository.getRankingRows(periodStart, prevEndExclusive, team);

    // 전체 순위 계산
    calculateRanks(currentAll);
    calculateRanks(prevAll);

    // 전체 diff 계산
    calculateDiff(currentAll, prevAll);

    //page, size 슬라이싱 작업
    int fromIndex = Math.max(0, (page - 1) * size);
    if (fromIndex >= currentAll.size()) {
      throw new BusinessException(ErrorCode.RANKING_NOT_FOUND,
              "요청한 페이지에 데이터가 없습니다.");
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
   * 현재 랭킹 / 1시간 전 랭킹 비교하여 diff 결정
   *  diff = 1시간 전 rank - 현재 rank (+순위 상승, -순위 하락, 0 변동x)
   */
  private void calculateDiff(List<RankingRowResponse> current,
                             List<RankingRowResponse> previous) {

    // 이전 랭킹을 userId를 기준으로 맵핑
    Map<Long, RankingRowResponse> prevByUserId = previous.stream()
            .collect(Collectors.toMap(
                    RankingRowResponse::getUserId,
                    r -> r
            ));

    for (RankingRowResponse curRow : current) {
      RankingRowResponse prevRow = prevByUserId.get(curRow.getUserId());

      if (prevRow == null || prevRow.getRank() == 0) {
        // 이전 랭킹에 없던 유저이거나 rank 정보 없으면 변동 없음(0)으로 처리
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


