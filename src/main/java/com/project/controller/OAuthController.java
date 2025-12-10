package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.dto.response.LoginResponse;
import com.project.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.success("Slack 소셜 로그인 성공", oAuthService.slackLogin(code)));
    }

    @GetMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(@RequestHeader(value = "refreshToken") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", oAuthService.reissueToken(refreshToken)));
    }
}
