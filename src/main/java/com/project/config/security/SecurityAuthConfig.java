package com.project.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.security.handler.JwtAccessDeniedHandler;
import com.project.common.security.handler.JwtAuthenticationEntryPoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SecurityAuthConfig {

  @Bean
  public PasswordEncoder bcryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
    return new JwtAccessDeniedHandler(objectMapper);
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return new JwtAuthenticationEntryPoint();
  }
}
