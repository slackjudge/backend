package com.project.security.jwt.access;

import com.project.common.annotation.AccessTokenStrategy;
import com.project.common.exception.ErrorCode;
import com.project.common.exception.JwtException;
import com.project.security.jwt.JwtClaims;
import com.project.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static com.project.security.jwt.access.AccessTokenClaimKeys.USER_ID;

@Slf4j
@Component
@AccessTokenStrategy
public class AccessTokenProvider implements JwtProvider {
    private final SecretKey secretKey;
    private final Duration tokenExpiration;

    public AccessTokenProvider(
            @Value("${jwt.secret.access-token.value}") String jwtSecretKey,
            @Value("${jwt.secret.access-token.expiration-time}") Duration tokenExpiration
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.tokenExpiration = tokenExpiration;
    }

    @Override
    public String createToken(JwtClaims claims) {
        Date now = new Date();

        return Jwts.builder()
                .setHeader(createHeader())
                .setClaims(claims.getClaims())
                .signWith(secretKey)
                .setExpiration(createExpirationDate(now, tokenExpiration.toMillis()))
                .compact();
    }

    @Override
    public JwtClaims parseJwtClaimsFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return AccessTokenClaim.of(
                Long.parseLong(claims.get(USER_ID.getValue(), String.class))
        );
    }

    @Override
    public LocalDateTime getExpiredDate(String token) {
        Claims claims = getClaimsFromToken(token);
        return Instant.ofEpochMilli(claims.getExpiration().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token is expired: {}", e.getMessage());
            throw new JwtException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }
    }

    @Override
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Token is invalid: {}", e.getMessage());
            throw new JwtException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Map<String, Object> createHeader() {
        return Map.of(
                "typ", "JWT",
                "alg", "HS256",
                "regDate", System.currentTimeMillis()
        );
    }

    private Date createExpirationDate(Date now, long expirationTime) {
        return new Date(now.getTime() + expirationTime);
    }
}
