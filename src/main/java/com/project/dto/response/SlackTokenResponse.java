package com.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SlackTokenResponse(
    boolean ok,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("id_token") String idToken,
    @JsonProperty("token_type") String tokenType,
    String error) {}
