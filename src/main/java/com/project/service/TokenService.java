package com.project.service;

import com.project.dto.response.LoginResponse;
import com.project.common.security.jwt.JwtClaims;
import com.project.common.security.jwt.JwtProvider;
import com.project.common.security.jwt.access.AccessTokenClaim;
import com.project.common.security.jwt.refresh.RefreshTokenClaim;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TokenService {

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
        return issueTokens(userId, true);
    }


    public void logout(Long userId, String refreshToken) {

        refreshTokenService.validateRefreshToken(userId, refreshToken);
        refreshTokenService.removeRefreshToken(userId);
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
