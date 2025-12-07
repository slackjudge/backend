package com.project.security;

public class WebSecurityUrl {

    private WebSecurityUrl() {
        throw new IllegalStateException("Utility class");
    }

    public static final String[] HEALTH_CHECK_ENDPOINT = {"/health"};
    public static final String[] READ_ONLY_PUBLIC_ENDPOINTS = {"/favicon.ico"};
    public static final String LOGIN_ENDPOINT = "/api/oauth/login";
    public static final String REISSUE_ENDPOINT = "/api/oauth/reissue";

    public static final String[] ANONYMOUS_ENDPOINTS = {LOGIN_ENDPOINT};
}
