package com.project.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.dto.ApiResponse;
import com.project.common.exception.ErrorCode;
import com.project.common.exception.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            if (!response.isCommitted()) {
                JwtException exception = JwtErrorCodeUtil.determineAuthErrorException(e);
                sendAuthError(response, exception);
            }
        }
    }

    private void sendAuthError(HttpServletResponse response, JwtException e) throws IOException {
        if (!response.isCommitted()) {

            ErrorCode errorCode = e.getErrorCode();

            ApiResponse<Void> apiResponse = ApiResponse.error(errorCode);
            String responseBody = objectMapper.writeValueAsString(apiResponse);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write(responseBody);
        }
    }
}
