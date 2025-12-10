package com.project.common.util;

public class WebSecurityUrl {

    private WebSecurityUrl() {
        throw new IllegalStateException("Utility class");
    }

    public static final String LOGIN_ENDPOINT = "/oauth/login*";
    public static final String REISSUE_ENDPOINT = "/oauth/reissue/**";
    private static final String[] HEALTH_CHECK_ENDPOINTS = {"/health", "/actuator/health"};
    private static final String[] READ_ONLY_PUBLIC_ENDPOINTS = {"/favicon.ico"};
    private static final String[] ANONYMOUS_ENDPOINTS = {LOGIN_ENDPOINT, REISSUE_ENDPOINT};

    public static String[] getHealthCheckEndpoints() {
        return HEALTH_CHECK_ENDPOINTS.clone();
    }

    public static String[] getReadOnlyPublicEndpoints() {
        return READ_ONLY_PUBLIC_ENDPOINTS.clone();
    }

    public static String[] getAnonymousEndpoints() {
        return ANONYMOUS_ENDPOINTS.clone();
    }
}
