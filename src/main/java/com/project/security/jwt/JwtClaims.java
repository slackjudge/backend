package com.project.security.jwt;

import java.util.Map;

public interface JwtClaims {
    Map<String, Object> getClaims();
}
