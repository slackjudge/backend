package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.service.DailyRankMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author 김경민
 */
@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/test/slack")
public class DailyRankMessageController {

    private final DailyRankMessageService dailyRankMessageService;

    /**==========================
    *
    * 일일 랭킹 메시지를 수동 전송한다.
    *
    * @parm -
    * @return ResponseEntity<ApiResponse<Void>>
    * @author 김경민
    * @version 1.0.0
    * @date 2025-12-17
    *
    ==========================**/
    @PostMapping("/daily-rank")
    public ResponseEntity<ApiResponse<Void>> sendDailyRank() {
        dailyRankMessageService.sendDailyRankMessage();
        return ResponseEntity.ok(ApiResponse.success("ok", null));
    }
}
