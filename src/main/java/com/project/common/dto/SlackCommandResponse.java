package com.project.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * @author 김경민
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackCommandResponse {

    @JsonProperty("response_type")
    private String responseType; // "ephemeral" | "in_channel"

    private String text;

    public static SlackCommandResponse ephemeral(String text) {
        return SlackCommandResponse.builder()
                .responseType("ephemeral")
                .text(text)
                .build();
    }

    public static SlackCommandResponse inChannel(String text) {
        return SlackCommandResponse.builder()
                .responseType("in_channel")
                .text(text)
                .build();
    }
}
