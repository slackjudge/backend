package com.project.common.util;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SlackMessageSender {

    private final Slack slack;

    private final String slackBotToken;

    // 실제 운영용 생성자
    @Autowired
    public SlackMessageSender(
            @Value("${slack.bot.token:slack-bot-token}") String slackBotToken
    ) {
        this.slack = Slack.getInstance();
        this.slackBotToken = slackBotToken;
    }

    // 테스트용 생성자
    public SlackMessageSender(Slack slack, String testToken) {
        this.slack = slack;
        this.slackBotToken = testToken;
    }

    public ChatPostMessageResponse sendMessage(String id, String message) throws IOException, SlackApiException {

        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(id)
                .text(message)
                .build();

        ChatPostMessageResponse response = slack.methods(slackBotToken).chatPostMessage(request);

        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.SLACK_MESSAGE_FAILED, "Slack 메시지 전송 실패: " + response.getError());
        }

        return response;
    }
}
