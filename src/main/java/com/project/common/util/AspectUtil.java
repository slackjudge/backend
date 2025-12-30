package com.project.common.util;

import com.project.common.exception.BusinessException;
import com.project.common.exception.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AspectUtil {

    private static final int MAX_LENGTH = 300;

    public void putCommon(JoinPoint joinPoint) {
        MDC.put("controller", joinPoint.getSignature().getDeclaringTypeName());
        MDC.put("method", joinPoint.getSignature().getName());
        MDC.put("userId", getCurrentUserId());
    }

    public void putCommonFromRequest(HttpServletRequest request) {
        MDC.put("path", request.getRequestURI());
        MDC.put("httpMethod", request.getMethod());
        MDC.put("userId", getCurrentUserId());
    }

    public void putSuccess(Object result) {
        MDC.put("phase", "SUCCESS");
        MDC.put("return", shorten(result));
    }

    public void putError(Throwable ex) {
        MDC.put("phase", "ERROR");

        if (ex instanceof JwtException je) {
            MDC.put("errorCode", je.getErrorCode().getCode());
            MDC.put("errorMessage", je.getErrorCode().getMessage());
        }
        else if (ex instanceof BusinessException be) {
            MDC.put("errorCode", be.getErrorCode().getCode());
            MDC.put("errorMessage", be.getErrorCode().getMessage());
        }
        else {
            MDC.put("errorCode", "UNEXPECTED");
            MDC.put("errorMessage", shorten(ex.getMessage()));
        }
    }

    public void clear() {
        MDC.clear();
    }


    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "anonymousUser";
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            return "anonymousUser";
        }

        return auth.getName();
    }

    private String shorten(Object obj) {
        if (obj == null) return "";
        String str = String.valueOf(obj);
        return (str.length() > MAX_LENGTH)
                ? str.substring(0, MAX_LENGTH) + "..."
                : str;
    }
}