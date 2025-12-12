package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.BojUtil;
import com.project.common.util.SlackUtil;
import com.project.common.util.TokenUtils;
import com.project.dto.request.LocalSignRequest;
import com.project.dto.response.LoginResponse;
import com.project.dto.response.SlackTokenResponse;
import com.project.dto.response.SlackUserInfoResponse;
import com.project.entity.UserEntity;
import com.project.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

  private final UserService userService;
  private final UserRepository userRepository;
  private final TokenUtils tokenUtils;
  private final SlackUtil slackUtil;
  private final RefreshTokenService refreshTokenService;
  private final BojUtil bojUtil;

  @Transactional
  public void localSign(LocalSignRequest localSignRequest) {
    UserEntity user = UserEntity.createUser("local");

    int bojTier = bojUtil.getBojTier(localSignRequest.baekjoonId());
    user.localSignUp(localSignRequest, bojTier);
    userRepository.save(user);
  }

  @Transactional
  public LoginResponse localLogin(String username) {
    UserEntity user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    return tokenUtils.issueTokens(user.getUserId(), true);
  }

  @Transactional
  public LoginResponse slackLogin(String code) {
    SlackTokenResponse slackTokenResponse = slackUtil.getSlackToken(code);
    SlackUserInfoResponse slackUserInfoResponse =
        slackUtil.getSlackUserInfo(slackTokenResponse.accessToken());
    UserEntity user = userService.findUserBySlackId(slackUserInfoResponse.userId());

    boolean registeredUser = !Objects.equals(user.getBaekjoonId(), "initial");

    return tokenUtils.issueTokens(user.getUserId(), registeredUser);
  }

  @Transactional
  public LoginResponse reissueToken(String refreshToken) {
    return tokenUtils.reissueToken(refreshToken);
  }

  @Transactional
  public void logout(Long userId) {
    refreshTokenService.removeRefreshToken(userId);
  }
}
