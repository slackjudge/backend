package com.project.config.security;

import com.project.common.security.filter.JwtAuthenticationFilter;
import com.project.common.security.filter.JwtExceptionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .exceptionHandling(
                        exception -> exception
                                .accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(authenticationEntryPoint)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "*").permitAll()
                        .requestMatchers(HttpMethod.GET, READ_ONLY_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(LOGIN_ENDPOINT).permitAll()
                        .requestMatchers(REISSUE_ENDPOINT).permitAll()
                        .requestMatchers(HEALTH_CHECK_ENDPOINT).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtExceptionFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
