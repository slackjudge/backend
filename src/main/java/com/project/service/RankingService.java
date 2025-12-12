package com.project.service;

import com.project.common.util.RankUtil;
import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.repository.RankingQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final RankingQueryRepository rankingQueryRepository;

  /**
   * 해당 시간대의 랭킹과 직전 시간대 랭킹을 비교하여 반환
   */
  public RankingPageResponse getRanking(String period, LocalDateTime dateTime, String group, int page, int size) {

    // 기준 시각 정규화 (null -> now, 분/초/나노 0)
    LocalDateTime baseTime = RankUtil.resolveBaseTime(dateTime);

    // 집계 시작 시각 (주의 월요일, 달의 첫째 날..) 2025-12-01T00:00:00
    LocalDateTime periodStart = RankUtil.getPeriodStart(period, baseTime);

    // [start, endExclusive) 구간 설정
    LocalDateTime currentEndExclusive = RankUtil.getPeriodEndExclusive(baseTime);              // ex) 15:00
    LocalDateTime prevEndExclusive    = RankUtil.getPeriodEndExclusive(baseTime.minusHours(1)); // ex) 14:00

//    // 현재/이전 구간 종료 시각 yyyy-MM-ddT14:00:00 ~ yyyy-MM-ddT13:00:00
//    LocalDateTime currentEnd = RankUtil.getCurrentEnd(baseTime);
//    LocalDateTime prevEnd = RankUtil.getPrevEnd(baseTime);


    // 현재 구간 랭킹 조회
    List<RankingRowResponse> currentAll = rankingQueryRepository.getRankingRows(periodStart, currentEndExclusive, group);

    // 이전 구간 랭킹 조회 (직전 한 시간 전까지)
    List<RankingRowResponse> prevAll = rankingQueryRepository.getRankingRows(periodStart, prevEndExclusive, group);

    // 전체 순위 계산
    calculateRanks(currentAll);
    calculateRanks(prevAll);

    // 전체 diff 계산
    calculateDiff(currentAll, prevAll);

    //page, size 슬라이싱 작업
    int fromIndex = Math.max(0, (page - 1) * size);
    if (fromIndex >= currentAll.size()) {
      return new RankingPageResponse(false, List.of());
    }

    int toIndex = Math.min(fromIndex + size, currentAll.size());
    List<RankingRowResponse> pageRows = currentAll.subList(fromIndex, toIndex);
    boolean hasNext = toIndex < currentAll.size();

    return new RankingPageResponse(hasNext, pageRows);
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


