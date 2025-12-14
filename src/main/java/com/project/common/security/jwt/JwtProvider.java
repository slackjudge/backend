package com.project.common.security.jwt;

import com.project.common.util.AuthConstants;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

public interface JwtProvider {

  default String resolveToken(String header) {
    if (StringUtils.hasText(header) && header.startsWith(AuthConstants.TOKEN_PREFIX.getValue())) {
      return header.substring(AuthConstants.TOKEN_PREFIX.getValue().length());
    }
    return "";
  }

  String createToken(JwtClaims claims);

  JwtClaims parseJwtClaimsFromToken(String token);

  LocalDateTime getExpiredDate(String token);

  boolean isTokenExpired(String token);

  Claims getClaimsFromToken(String token);
}
