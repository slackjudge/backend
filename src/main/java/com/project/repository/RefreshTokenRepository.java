package com.project.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret.refresh-token.key-prefix}")
    private String keyPrefix;

    @Value("${jwt.secret.refresh-token.expiration-time}")
    private Duration refreshTokenExpiration;

    public void save(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                getKey(userId),
                refreshToken,
                refreshTokenExpiration
        );
    }

    public String find(String userId) {
        return redisTemplate.opsForValue().get(getKey(userId));
    }

    public void delete(String userId) {
        redisTemplate.delete(getKey(userId));
    }

    private String getKey(String userId) {
        return keyPrefix + userId;
    }
}
