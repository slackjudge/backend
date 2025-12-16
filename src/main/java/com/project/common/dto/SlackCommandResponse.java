package com.project.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackCommandResponse {

    private String response_type; // "ephemeral" | "in_channel"

    private String text;

    public static SlackCommandResponse ephemeral(String text) {
        return SlackCommandResponse.builder()
                .response_type("ephemeral")
                .text(text)
                .build();
    }

    public static SlackCommandResponse inChannel(String text) {
        return SlackCommandResponse.builder()
                .response_type("in_channel")
                .text(text)
                .build();
    }
}
