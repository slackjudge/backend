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

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyRankMessageResponse>>> getNotifications(
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(lastId, size)));
    }
}
