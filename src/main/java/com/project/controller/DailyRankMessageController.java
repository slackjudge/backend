package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.service.DailyRankMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class DailyRankMessageController {

    private final DailyRankMessageService dailyRankMessageService;

    @PostMapping("/daily-rank")
    public ResponseEntity<ApiResponse<Void>> sendDailyRank() {
        dailyRankMessageService.sendDailyRankMessage();
        return ResponseEntity.ok(ApiResponse.success("ok", null));
    }
}
