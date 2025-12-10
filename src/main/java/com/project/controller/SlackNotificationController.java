package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class SlackNotificationController {

    private final SlackNotificationService slackNotificationService;

    @PostMapping("/daily-rank")
    public ResponseEntity<ApiResponse<Void>> sendDailyRank() {
        slackNotificationService.sendDailyRankMessage();
        return ResponseEntity.ok(ApiResponse.success("ok", null));
    }


}
