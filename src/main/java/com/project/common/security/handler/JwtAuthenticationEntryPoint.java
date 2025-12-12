package com.project.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.dto.ApiResponse;
import com.project.common.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

    ApiResponse<Void> apiResponse = ApiResponse.error(errorCode);
    String responseBody = objectMapper.writeValueAsString(apiResponse);

    response.setStatus(errorCode.getStatus().value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(responseBody);
  }
}
