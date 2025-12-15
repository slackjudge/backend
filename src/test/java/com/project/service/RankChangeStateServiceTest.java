package com.project.service;

import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import com.project.dto.RankRawData;
import com.project.entity.RankChangeStateEntity;
import com.project.entity.UserEntity;
import com.project.repository.RankChangeStateRepository;
import com.project.repository.UserRepository;
import com.project.repository.UsersProblemRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class RankChangeStateServiceTest {
    @Mock
    SlackMessageSender slackMessageSender;

    @Mock
    MessageFormatUtil messageFormatUtil;

    @Mock
    UserRepository userRepository;

    @Mock
    UsersProblemRepository usersProblemRepository;

    @Mock
    RankChangeStateRepository rankChangeStateRepository;

    @InjectMocks
    RankChangeStateService rankChangeStateService;

    @Test
    @DisplayName("순위 상승 시 DM 전송 및 상태 업데이트")
    void rankUp_sendDm_and_updateState() throws Exception {
        // given
        RankRawData row = new RankRawData(1L, "유재석", 5L, 200L);
        when(usersProblemRepository.findMonthlyRank(any()))
                .thenReturn(List.of(row));

        RankChangeStateEntity state = RankChangeStateEntity.create(1L, 5); // 이전 5위
        when(rankChangeStateRepository.findAllById(any())).thenReturn(List.of(state));

        UserEntity user = UserEntity.builder()
                .userId(1L)
                .slackId("U1")
                .username("유재석")
                .isAlertAgreed(true)
                .build();

        when(userRepository.findAllById(any())).thenReturn(List.of(user));

        when(messageFormatUtil.formatRankChange(any(), anyInt(), anyInt(), anyLong()))
                .thenReturn("RANK_UP");

        when(slackMessageSender.sendMessage(any(), any()))
                .thenReturn(new ChatPostMessageResponse());

        // when
        rankChangeStateService.sendRankChangeMessage();

        // then
        verify(slackMessageSender).sendMessage("U1", "RANK_UP");
        verify(rankChangeStateRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("최초 유저는 기준만 저장하고 알림은 보내지 않는다")
    void firstUser_onlySaveState() throws SlackApiException, IOException {
        // given
        RankRawData row = new RankRawData(1L, "유재석", 5L, 100L);
        when(usersProblemRepository.findMonthlyRank(any())).thenReturn(List.of(row));

        when(rankChangeStateRepository.findAllById(any())).thenReturn(List.of());

        // when
        rankChangeStateService.sendRankChangeMessage();

        // then
        verify(rankChangeStateRepository).saveAll(anyList());
        verify(slackMessageSender, never()).sendMessage(any(), any());
    }

    @Test
    @DisplayName("순위 유지 또는 하락 시 알림 없음, 상태만 업데이트")
    void rankNotUp_noDm() throws SlackApiException, IOException {
        // given
        RankRawData row = new RankRawData(1L, "유재석", 5L, 100L);
        when(usersProblemRepository.findMonthlyRank(any()))
                .thenReturn(List.of(row));

        RankChangeStateEntity state = RankChangeStateEntity.create(1L, 1);
        when(rankChangeStateRepository.findAllById(any()))
                .thenReturn(List.of(state));

        UserEntity user = UserEntity.builder()
                .userId(1L)
                .slackId("U1")
                .username("유재석")
                .isAlertAgreed(true)
                .build();

        when(userRepository.findAllById(any()))
                .thenReturn(List.of(user));

        // when
        rankChangeStateService.sendRankChangeMessage();

        // then
        verify(slackMessageSender, never()).sendMessage(any(), any());
        verify(rankChangeStateRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("알림 미동의 유저는 순위가 상승해도 DM을 보내지 않는다")
    void notAgreed_noDm() throws SlackApiException, IOException {
        // given
        RankRawData row = new RankRawData(1L, "유재석", 5L, 200L);
        when(usersProblemRepository.findMonthlyRank(any()))
                .thenReturn(List.of(row));

        RankChangeStateEntity state = RankChangeStateEntity.create(1L, 5);
        when(rankChangeStateRepository.findAllById(any()))
                .thenReturn(List.of(state));

        UserEntity user = UserEntity.builder()
                .userId(1L)
                .slackId("U1")
                .username("유재석")
                .isAlertAgreed(false)
                .build();

        when(userRepository.findAllById(any()))
                .thenReturn(List.of(user));

        // when
        rankChangeStateService.sendRankChangeMessage();

        // then
        verify(slackMessageSender, never()).sendMessage(any(), any());
        verify(rankChangeStateRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("DM 실패해도 상태는 업데이트된다.")
    void dmFail_notUpdateState() throws Exception {
        // given
        RankRawData row = new RankRawData(1L, "유재석", 5L, 200L);
        when(usersProblemRepository.findMonthlyRank(any()))
                .thenReturn(List.of(row));

        RankChangeStateEntity state = RankChangeStateEntity.create(1L, 5);
        when(rankChangeStateRepository.findAllById(any()))
                .thenReturn(List.of(state));

        UserEntity user = UserEntity.builder()
                .userId(1L)
                .slackId("U1")
                .username("유재석")
                .isAlertAgreed(true)
                .build();

        when(userRepository.findAllById(any()))
                .thenReturn(List.of(user));

        when(slackMessageSender.sendMessage(any(), any()))
                .thenThrow(new RuntimeException("slack error"));

        // when
        rankChangeStateService.sendRankChangeMessage();

        // then
        verify(rankChangeStateRepository).saveAll(anyList());
    }
}
