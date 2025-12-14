package com.project.common.util;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SlackMessageSenderTest {
    @Test
    @DisplayName("Slack 메시지 전송 성공 테스트")
    void sendMessageSuccess() throws Exception {
        // given
        Slack slack = mock(Slack.class);
        MethodsClient methods = mock(MethodsClient.class);

        SlackMessageSender sender = new SlackMessageSender(slack, "TEST_TOKEN");

        ChatPostMessageResponse response = new ChatPostMessageResponse();
        response.setOk(true);

        when(slack.methods("TEST_TOKEN")).thenReturn(methods);
        when(methods.chatPostMessage(any(ChatPostMessageRequest.class)))
                .thenReturn(response);

        // when
        ChatPostMessageResponse result = sender.sendMessage("C01", "hello");

        // then
        assertThat(result.isOk()).isTrue();
        verify(slack, times(1)).methods("TEST_TOKEN");
        verify(methods, times(1)).chatPostMessage(any(ChatPostMessageRequest.class));
    }

    @Test
    @DisplayName("Slack 메시지 실패 시 BusinessException 발생")
    void sendMessageFail() throws Exception {
        // given
        Slack slack = mock(Slack.class);
        MethodsClient methods = mock(MethodsClient.class);

        SlackMessageSender sender = new SlackMessageSender(slack, "TEST_TOKEN");

        ChatPostMessageResponse failResponse = new ChatPostMessageResponse();
        failResponse.setOk(false);
        failResponse.setError("invalid_auth");

        when(slack.methods("TEST_TOKEN")).thenReturn(methods);
        when(methods.chatPostMessage(any(ChatPostMessageRequest.class)))
                .thenReturn(failResponse);

        // when & then
        assertThatThrownBy(() -> sender.sendMessage("C01", "hello"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SLACK_MESSAGE_FAILED);
        verify(methods, times(1)).chatPostMessage(any(ChatPostMessageRequest.class));
    }
}
