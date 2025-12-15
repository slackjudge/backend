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
   */
  public RankingPageResponse getRanking(String period, LocalDateTime dateTime, String group, int page, int size) {
    // period 값 검증 period=invaild -> 예외
    String normalizedPeriod = normalizeAndValidatePeriod(period);

    // group 값 검증, null or All -> null, 그외 enum값 매핑, 매핑 안될 경우 예외
    EurekaTeamName team = parseAndValidateGroup(group);

    // 페이징 파라미터 유효성 검사
    validatePagination(page, size);

    // 도커 컨테이너 타임존 한국 시간으로 변경
    LocalDateTime effective = (dateTime != null) ? dateTime : LocalDateTime.now(clock);

    // 기준 시각 정규화 (null -> now, 분/초/나노 0) 14:39 -> 14:00으로 절삭
    LocalDateTime baseTime = RankUtil.resolveBaseTime(effective);

    // 현재 서버의 시각
    LocalDateTime now = LocalDateTime.now(clock);

    // 집계 시작 시각 (주의 월요일, 달의 첫째 날..) 2025-12-01T00:00:00
    LocalDateTime periodStart = RankUtil.getPeriodStart(normalizedPeriod, baseTime);


    // 종료 시점은 period + now에 따라서 변경
    LocalDateTime currentEndExclusive = RankUtil.getPeriodEndExclusive(normalizedPeriod, baseTime, now);              // ex) 15:00
    LocalDateTime prevEndExclusive    = currentEndExclusive.minusHours(1); // ex) 14:00


    // 현재 구간 랭킹 조회
    List<RankingRowResponse> currentAll = rankingQueryRepository.getRankingRows(periodStart, currentEndExclusive, team);

    // 해당 기간/그룹에 문제를 푼 사람이 없을 경우 -> 정상처리 후 -> 빈 배열 반환
    if (currentAll.isEmpty()) {
       return new RankingPageResponse(false, List.of());
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
    // 프론트에서 raceCondition으로 인해 무한 재로딩 -> 빈 배열 반환 후 프론트에서 직접 처리
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


