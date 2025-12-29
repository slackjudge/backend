package com.project.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ActuatorAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final String monitoringHost;

    public ActuatorAuthorizationManager(@Value("${LOKI_URI:localhost}") String monitoringHost) {
        this.monitoringHost = monitoringHost;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String remoteAddr = request.getRemoteAddr();

        boolean isAllowed = isLocalhost(remoteAddr) || monitoringHost.equals(remoteAddr);

        return new AuthorizationDecision(isAllowed);
    }

    private boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip);
    }
}
