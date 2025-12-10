package com.project.service;

import com.project.common.util.TokenUtils;
import com.project.common.util.SlackUtil;
import com.project.dto.request.LogoutRequest;
import com.project.dto.response.LoginResponse;
import com.project.dto.response.SlackTokenResponse;
import com.project.dto.response.SlackUserInfoResponse;
import com.project.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserService userService;
    private final TokenUtils tokenUtils;
    private final SlackUtil slackUtil;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse slackLogin(String code) {
        SlackTokenResponse slackTokenResponse = slackUtil.getSlackToken(code);
        SlackUserInfoResponse slackUserInfoResponse = slackUtil.getSlackUserInfo(slackTokenResponse.accessToken());
        UserEntity user = userService.findUserBySlackId(slackUserInfoResponse.userId());

        boolean registeredUser = !Objects.equals(user.getBaekjoonId(), "initial");

        return tokenUtils.issueTokens(user.getUserId(), registeredUser);
    }

    @Transactional
    public LoginResponse reissueToken(String refreshToken) {
        return tokenUtils.reissueToken(refreshToken);
    }

    @Transactional
    public void logout(Long userId, LogoutRequest logoutRequest) {
        refreshTokenService.validateRefreshToken(userId, logoutRequest.refreshToken());
        refreshTokenService.removeRefreshToken(userId);
    }
}
