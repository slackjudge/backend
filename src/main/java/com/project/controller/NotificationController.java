package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.dto.response.DailyRankMessageResponse;
import com.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 김경민
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**==========================
    *
    * 알림 목록을 조회한다.
    *
    * @parm lastId 마지막 메시지 ID
    * @parm size 조회 건수
    * @return ResponseEntity<ApiResponse<List<DailyRankMessageResponse>>>
    * @author 김경민
    * @version 1.0.0
    * @date 2025-12-15
    *
    ==========================**/
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyRankMessageResponse>>> getNotifications(
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(lastId, size)));
    }
}
