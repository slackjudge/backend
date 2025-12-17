package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.service.RankChangeStateService;
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
public class RankChangeStateController {

    private final RankChangeStateService rankChangeStateService;

    /**==========================
    *
    * 순위 변동 알림을 수동 전송한다.
    *
    * @parm -
    * @return ResponseEntity<ApiResponse<Void>>
    * @author 김경민
    * @version 1.0.0
    * @date 2025-12-14
    *
    ==========================**/
    @PostMapping("/rank-change")
    public ResponseEntity<ApiResponse<Void>> sendRankChange() {
        rankChangeStateService.sendRankChangeMessage();
        return ResponseEntity.ok(ApiResponse.success("ok", null));
    }
}
