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

import static com.project.common.util.WebSecurityUrl.getReadOnlyPublicEndpoints;
import static com.project.common.util.WebSecurityUrl.getHealthCheckEndpoints;
import static com.project.common.util.WebSecurityUrl.LOGIN_ENDPOINT;
import static com.project.common.util.WebSecurityUrl.REISSUE_ENDPOINT;
import static com.project.common.util.WebSecurityUrl.LOCAL_LOGIN_ENDPOINT;
import static com.project.common.util.WebSecurityUrl.LOCAL_SIGN_ENDPOINT;

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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, getReadOnlyPublicEndpoints()).permitAll()
                        .requestMatchers(getHealthCheckEndpoints()).permitAll()
                        .requestMatchers(LOCAL_LOGIN_ENDPOINT).permitAll()
                        .requestMatchers(LOCAL_SIGN_ENDPOINT).permitAll()
                        .requestMatchers(LOGIN_ENDPOINT).permitAll()
                        .requestMatchers(REISSUE_ENDPOINT).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
