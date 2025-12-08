package com.project.common.security.filter;

import com.project.common.exception.ErrorCode;
import com.project.common.exception.JwtException;
import com.project.common.security.jwt.JwtProvider;
import com.project.common.security.jwt.JwtClaims;
import com.project.common.security.jwt.access.AccessTokenClaimKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.project.common.util.WebSecurityUrl.*;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService securityUserDetailsService;
    private final JwtProvider accessTokenProvider;
    private final AccessDeniedHandler accessDeniedHandler;

    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static final String[] PUBLIC_ENDPOINTS = Stream.of(
            HEALTH_CHECK_ENDPOINT,
            READ_ONLY_PUBLIC_ENDPOINTS
    ).flatMap(Arrays::stream).toArray(String[]::new);


    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (isAnonymousRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String accessToken = resolveAccessToken(request);

            UserDetails userDetails = getUserDetails(accessToken);
            authenticateUser(userDetails, request);
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException e) {
            accessDeniedHandler.handle(request, response, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return matches(uri, PUBLIC_ENDPOINTS)   // 완전 공개 API
                || matches(uri, ANONYMOUS_ENDPOINTS); // 토큰 없어도 허용되는 API
    }

    private boolean isAnonymousRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean headerMissing = request.getHeader(HttpHeaders.AUTHORIZATION) == null;

        return headerMissing && matches(uri, ANONYMOUS_ENDPOINTS);
    }

    private String resolveAccessToken(
            HttpServletRequest request
    ) throws ServletException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = accessTokenProvider.resolveToken(authHeader);

        if (!StringUtils.hasText(accessToken)) {
            log.error("EMPTY_ACCESS_TOKEN");
            throw new JwtException(ErrorCode.EMPTY_ACCESS_TOKEN);
        }

        if (accessTokenProvider.isTokenExpired(accessToken)) {
            log.error("TOKEN_EXPIRED");
            throw new JwtException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }

        return accessToken;
    }

    private UserDetails getUserDetails(String accessToken) {
        JwtClaims claims = accessTokenProvider.parseJwtClaimsFromToken(accessToken);
        String userId = (String) claims.getClaims().get(AccessTokenClaimKeys.USER_ID.getValue());
        return securityUserDetailsService.loadUserByUsername(userId);
    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private boolean matches(String uri, String[] patterns) {
        for (String pattern : patterns) {
            if (matcher.match(pattern, uri)) return true;
        }
        return false;
    }
}
