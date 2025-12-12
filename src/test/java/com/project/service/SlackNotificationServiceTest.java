package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import com.project.dto.DailyRankInfo;
import com.project.dto.DailyRankRawData;
import com.project.repository.DailyRankMessageRepository;
import com.project.repository.UsersProblemRepository;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SlackNotificationServiceTest {

    @Mock
    SlackMessageSender slackMessageSender;

    @Mock
    MessageFormatUtil messageFormatUtil;

    @Mock
    UsersProblemRepository usersProblemRepository;

    @Mock
    DailyRankMessageRepository dailyRankMessageRepository;

    @InjectMocks
    SlackNotificationService slackNotificationService;

    @Test
    @DisplayName("일일 랭킹 알림 메시지 전송 검증")
    void sendDailyRankMessage() throws Exception {
        List<DailyRankRawData> raw = List.of(new DailyRankRawData(1L, "유재석", 7L, 48L));

        List<DailyRankInfo> ranked = List.of(
                new DailyRankInfo("유재석", 7, 48, 1)
        );

        when (usersProblemRepository.findDailyRank(any(), any())).thenReturn(raw);
        when (messageFormatUtil.formatDailyRank(any())).thenReturn("TEST_FORMATTED_MESSAGE");

        ChatPostMessageResponse mockResponse = new ChatPostMessageResponse();
        mockResponse.setOk(true);

        when (slackMessageSender.sendMessage(anyString(), anyString())).thenReturn(mockResponse);

        slackNotificationService.sendDailyRankMessage();

        verify(slackMessageSender, times(1)).sendMessage("C0A0M8HUQDT", "TEST_FORMATTED_MESSAGE");
        verify(dailyRankMessageRepository).save(any());
    }

    @Test
    @DisplayName("일일 랭킹 데이터가 없을 경우 기본 메시지 전송")
    void sendDailyRankMessage_empty() throws Exception {
        when(usersProblemRepository.findDailyRank(any(), any())).thenReturn(List.of());

        ChatPostMessageResponse response = new ChatPostMessageResponse();
        response.setOk(true);

        when(slackMessageSender.sendMessage(anyString(), anyString())).thenReturn(response);

        slackNotificationService.sendDailyRankMessage();

        verify(slackMessageSender).sendMessage(("C0A0M8HUQDT"), contains("새로운 문제 풀이가 없습니다"));
    }

    @Test
    @DisplayName("개인 순위 변동 메시지 전송 검증")
    void sendRankChangeMessage() throws Exception {
        when(messageFormatUtil.formatRankChange(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn("RANK_CHANGED");

        ChatPostMessageResponse mockResponse = new ChatPostMessageResponse();
        mockResponse.setOk(true);

        when(slackMessageSender.sendMessage(anyString(), anyString()))
                .thenReturn(mockResponse);

        slackNotificationService.sendRankChangeMessage();

        verify(slackMessageSender).sendMessage("U0A1NG7GEA2", "RANK_CHANGED");
    }

    @Test
    @DisplayName("DailyRankMessage - BusinessException 발생 시 그대로 throw")
    void dailyRankMessage_businessException() throws Exception {

        when(messageFormatUtil.formatDailyRank(any()))
                .thenReturn("TEXT");

        when(slackMessageSender.sendMessage(anyString(), anyString()))
                .thenThrow(new BusinessException(ErrorCode.SLACK_MESSAGE_FAILED));

        assertThatThrownBy(() -> slackNotificationService.sendDailyRankMessage())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SLACK_MESSAGE_FAILED.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SLACK_MESSAGE_FAILED);
    }

    @Test
    @DisplayName("DailyRankMessage - 일반 Exception 발생 시 BusinessException으로 변환")
    void dailyRankMessage_generalException() throws Exception {

        when(messageFormatUtil.formatDailyRank(any()))
                .thenReturn("TEXT");

        when(slackMessageSender.sendMessage(anyString(), anyString()))
                .thenThrow(new RuntimeException("unknown error"));

        assertThatThrownBy(() -> slackNotificationService.sendDailyRankMessage())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SLACK_MESSAGE_FAILED);
    }
}
