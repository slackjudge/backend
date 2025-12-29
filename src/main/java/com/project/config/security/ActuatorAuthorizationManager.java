package com.project.config.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@Component
public class ActuatorAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final String monitoringHost;
    private final Set<String> allowedIps = new HashSet<>();

    public ActuatorAuthorizationManager(@Value("${LOKI_URI:localhost}") String monitoringHost) {
        this.monitoringHost = monitoringHost;
    }

    @PostConstruct
    public void init() {
        allowedIps.add("127.0.0.1");
        allowedIps.add("0:0:0:0:0:0:0:1");

        try {
            InetAddress[] addresses = InetAddress.getAllByName(monitoringHost);
            for (InetAddress address : addresses) {
                allowedIps.add(address.getHostAddress());
            }
            log.info("Monitoring host '{}' resolved to IPs: {}", monitoringHost, allowedIps);
        } catch (UnknownHostException e) {
            log.warn("Failed to resolve monitoring host '{}', using as-is", monitoringHost);
            allowedIps.add(monitoringHost);
        }
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String clientIp = getClientIp(request);

        boolean isAllowed = allowedIps.contains(clientIp);

        return new AuthorizationDecision(isAllowed);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
