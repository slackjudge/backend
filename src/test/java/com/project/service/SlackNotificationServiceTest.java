package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SlackNotificationServiceTest {

    @Mock
    SlackMessageSender sender;

    @Mock
    MessageFormatUtil formatUtil;

    @InjectMocks
    SlackNotificationService service;

    @Test
    @DisplayName("일일 랭킹 알림 메시지 전송 검증")
    void sendDailyRankMessage() throws Exception {
        when(formatUtil.formatDailyRank(any(), any(), any()))
                .thenReturn("TEST_FORMATTED_MESSAGE");

        ChatPostMessageResponse mockResponse = new ChatPostMessageResponse();
        mockResponse.setOk(true);

        when(sender.sendMessage(any(), any())).thenReturn(mockResponse);

        service.sendDailyRankMessage();

        verify(sender, times(1)).sendMessage("C0A0M8HUQDT", "TEST_FORMATTED_MESSAGE");
    }

    @Test
    @DisplayName("개인 순위 변동 메시지 전송 검증")
    void sendRankChangeMessage() throws Exception {
        when(formatUtil.formatRankChange(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn("RANK_CHANGED");

        ChatPostMessageResponse mockResponse = new ChatPostMessageResponse();
        mockResponse.setOk(true);

        when(sender.sendMessage(anyString(), anyString()))
                .thenReturn(mockResponse);

        service.sendRankChangeMessage();

        verify(sender).sendMessage("U0A1NG7GEA2", "RANK_CHANGED");
    }

    @Test
    @DisplayName("DailyRankMessage - BusinessException 발생 시 그대로 throw")
    void dailyRankMessage_businessException() throws Exception {

        when(formatUtil.formatDailyRank(any(), any(), any()))
                .thenReturn("TEXT");

        when(sender.sendMessage(anyString(), anyString()))
                .thenThrow(new BusinessException(ErrorCode.SLACK_MESSAGE_FAILED));

        assertThatThrownBy(() -> service.sendDailyRankMessage())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SLACK_MESSAGE_FAILED.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SLACK_MESSAGE_FAILED);
    }

    @Test
    @DisplayName("DailyRankMessage - 일반 Exception 발생 시 BusinessException으로 변환")
    void dailyRankMessage_generalException() throws Exception {

        when(formatUtil.formatDailyRank(any(), any(), any()))
                .thenReturn("TEXT");

        when(sender.sendMessage(anyString(), anyString()))
                .thenThrow(new RuntimeException("unknown error"));

        assertThatThrownBy(() -> service.sendDailyRankMessage())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SLACK_MESSAGE_FAILED);
    }
}
