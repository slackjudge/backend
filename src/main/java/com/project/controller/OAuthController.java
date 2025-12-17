package com.project.controller;

import com.project.common.dto.ApiResponse;
import com.project.common.security.SecurityUserDetails;
import com.project.dto.response.LoginResponse;
import com.project.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.success("Slack 소셜 로그인 성공", oAuthService.slackLogin(code)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(@RequestHeader(value = "refreshToken") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", oAuthService.reissueToken(refreshToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal SecurityUserDetails userInfo) {
        oAuthService.logout(userInfo.getId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }
}
