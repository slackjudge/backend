package com.project.common.security.jwt.refresh;

import com.project.common.annotation.RefreshTokenStrategy;
import com.project.common.exception.ErrorCode;
import com.project.common.exception.JwtException;
import com.project.common.security.jwt.JwtClaims;
import com.project.common.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static com.project.common.security.jwt.refresh.RefreshTokenClaimKeys.USER_ID;

@Slf4j
@Component
@RefreshTokenStrategy
public class RefreshTokenProvider implements JwtProvider {
    private final SecretKey secretKey;
    private final Long tokenExpiration;

    public RefreshTokenProvider(
            @Value("${jwt.secret.refresh-token.value}") String jwtSecretKey,
            @Value("${jwt.secret.refresh-token.expiration-time}") Long tokenExpiration
    ) {
        final byte[] secretKeyBytes = Base64.getDecoder().decode(jwtSecretKey);
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        this.tokenExpiration = tokenExpiration;
    }


    @Override
    public String createToken(JwtClaims claims) {
        Date now = new Date();

        return Jwts.builder()
                .setHeader(createHeader())
                .setClaims(claims.getClaims())
                .signWith(secretKey)
                .setExpiration(createExpireDate(now, tokenExpiration))
                .compact();
    }

    @Override
    public JwtClaims parseJwtClaimsFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return RefreshTokenClaim.of(
                Long.parseLong(claims.get(USER_ID.getValue(), String.class))
        );
    }

    @Override
    public LocalDateTime getExpiredDate(String token) {
        Claims claims = getClaimsFromToken(token);
        return Instant.ofEpochMilli(claims.getExpiration().getTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token is Expired: {}", e.getMessage());
            throw new JwtException(ErrorCode.EXPIRED_REFRESH_TOKEN);
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
            log.error("Token parsing error: {}", e.getMessage());
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

    private Date createExpireDate(final Date now, long expirationTime) {
        return new Date(now.getTime() + expirationTime);
    }
}
