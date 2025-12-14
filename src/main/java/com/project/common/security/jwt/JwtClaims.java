package com.project.common.security.jwt;

import java.util.Map;

public interface JwtClaims {
    Map<String, Object> getClaims();
}
