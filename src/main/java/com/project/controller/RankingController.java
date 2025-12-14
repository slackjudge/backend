package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.dto.response.RankingPageResponse;
import com.project.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/rank")
@Slf4j
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  /**
   * 랭킹 조회 API
   * @param period day, week, month (default: day)
   * @param dateTime 기준 시각 (없으면 요청 시각 기준)
   * @param group 그룹 (default: ALL)
   * @param page 페이지 번호 (1부터 시작)
   * @param size 페이지 크기
   */
  @GetMapping
  public ResponseEntity<ApiResponse<RankingPageResponse>> getRanking(
          @RequestParam(defaultValue = "day") String period,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateTime,
          @RequestParam(defaultValue = "ALL") String group,
          @RequestParam(defaultValue = "1") int page,
          @RequestParam(defaultValue = "20") int size) {
    RankingPageResponse response = rankingService.getRanking(period, dateTime, group, page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
