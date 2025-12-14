package com.project.common.util;

import com.project.common.security.jwt.JwtClaims;
import com.project.common.security.jwt.JwtProvider;
import com.project.common.security.jwt.access.AccessTokenClaim;
import com.project.common.security.jwt.refresh.RefreshTokenClaim;
import com.project.dto.response.LoginResponse;
import com.project.service.RefreshTokenService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenUtils {

  private final JwtProvider accessTokenProvider;
  private final JwtProvider refreshTokenProvider;
  private final RefreshTokenService refreshTokenService;

  public LoginResponse issueTokens(Long userId, boolean registeredUser) {
    String accessToken = accessTokenProvider.createToken(AccessTokenClaim.of(userId));
    String refreshToken = refreshTokenProvider.createToken(RefreshTokenClaim.of(userId));

    refreshTokenService.saveRefreshToken(userId, refreshToken);
    return new LoginResponse(accessToken, refreshToken, registeredUser);
  }

  public LoginResponse reissueToken(String oldRefreshToken) {
    Long userId = resolveRefreshToken(oldRefreshToken);
    refreshTokenService.validateRefreshToken(userId, oldRefreshToken);
    refreshTokenService.removeRefreshToken(userId);

    return issueTokens(userId, true);
  }

  private Long resolveRefreshToken(String refreshToken) {
    JwtClaims claims = refreshTokenProvider.parseJwtClaimsFromToken(refreshToken);
    return getClaimValue(claims, "id", Long::parseLong);
  }

  private static <T> T getClaimValue(JwtClaims claims, String key, Function<String, T> converter) {
    Object value = claims.getClaims().get(key);
    if (value != null) {
      return converter.apply((String) value);
    }
    return null;
  }
}
