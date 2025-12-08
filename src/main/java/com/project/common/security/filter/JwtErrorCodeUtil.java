package com.project.common.security.filter;

import com.project.common.exception.ErrorCode;
import com.project.common.exception.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SignatureException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtErrorCodeUtil {

    private static final Map<Class<? extends Exception>, ErrorCode> ERROR_CODE_MAP = Map.of(
            ExpiredJwtException.class, ErrorCode.EXPIRED_ACCESS_TOKEN,
            MalformedJwtException.class, ErrorCode.MALFORMED_TOKEN,
            SignatureException.class, ErrorCode.SIGNATURE_TOKEN,
            UnsupportedJwtException.class, ErrorCode.UNSUPPORTED_JWT_TOKEN
    );

    public static JwtException determineAuthErrorException(Exception exception) {
        return findAuthErrorException(exception).orElseGet(
                () -> {
                    ErrorCode errorCode = determineErrorCode(exception, ErrorCode.UNAUTHORIZED);
                    log.debug(exception.getMessage(), exception);
                    return new JwtException(errorCode);
                }
        );
    }

    public static ErrorCode determineErrorCode(Exception exception, ErrorCode defaultErrorCode) {
        if (exception instanceof JwtException jwtException)
            return jwtException.getErrorCode();

        Class<? extends Exception> exceptionClass = exception.getClass();
        return ERROR_CODE_MAP.getOrDefault(exceptionClass, defaultErrorCode);
    }

    private static Optional<JwtException> findAuthErrorException(Exception exception) {
        if (exception instanceof JwtException) {
            return Optional.of((JwtException)exception);
        } else if (exception.getCause() instanceof JwtException) {
            return Optional.of((JwtException)exception.getCause());
        }
        return Optional.empty();
    }
}
