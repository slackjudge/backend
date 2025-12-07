package com.project.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "Invalid input"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "Resource not found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),

    // Users
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USERS_001", "User not found"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "Access denied"),

    // Jwt
    EMPTY_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "Access Token is empty"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.FORBIDDEN, "AUTH_004", "Access Token is expired"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "AUTH_005", "Refresh Token is expired"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_006", "Refresh Token not found"),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_007", "RefreshToken is mismatch"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_008", "Token is invalid"),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_009", "Token is abnormal"),
    SIGNATURE_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_010", "Token is manipulated"),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_011", "Token is unsupported"),
    SLACK_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_020", "Slack authentication failed"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
