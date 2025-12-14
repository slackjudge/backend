package com.project.service;

import com.project.common.exception.ErrorCode;
import com.project.common.exception.JwtException;
import com.project.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenRepository.save(String.valueOf(userId), refreshToken);
    }

    public void removeRefreshToken(Long userId) {
        refreshTokenRepository.delete(String.valueOf(userId));
    }

    public void validateRefreshToken(Long userId, String expectedRefreshToken) {
        String refreshToken = refreshTokenRepository.find(String.valueOf(userId));
        if (!refreshToken.equals(expectedRefreshToken)) {
            throw new JwtException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
    }
}
