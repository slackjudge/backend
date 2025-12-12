package com.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SlackUserInfoResponse(
    boolean ok,
    String sub,
    @JsonProperty("https://slack.com/user_id") String userId,
    @JsonProperty("https://slack.com/team_id") String teamId,
    String email,
    String name,
    String picture,
    String locale,
    @JsonProperty("https://slack.com/team_name") String teamName) {}
