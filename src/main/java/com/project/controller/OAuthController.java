package com.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.common.dto.ApiResponse;
import com.project.dto.response.LoginResponse;
import com.project.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.success(oAuthService.slackLogin(code)));
    }
}
