package com.project.controller;

import com.project.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rank")
@Slf4j
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  /**
   * param period : day, week, month / default : day date : 날짜 / default : group : 그룹 / default :
   * ALL page : 페이지 번호 / default : 1 size : 페이지 크기 / default : ??? 프론트 화면 보고 결정
   */
}
