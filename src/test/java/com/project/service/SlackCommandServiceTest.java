package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.entity.UserEntity;
import com.project.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackCommandServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    SlackCommandService slackCommandService;

    @Test
    @DisplayName("/notify on → 알림이 켜진다")
    void notify_on() {
        // given
        UserEntity user = UserEntity.builder()
                .slackId("U123")
                .isAlertAgreed(false)
                .build();

        when(userRepository.findBySlackId("U123"))
                .thenReturn(Optional.of(user));

        // when
        String result = slackCommandService.handleNotify("on", "U123");

        // then
        assertThat(result).contains("켜졌습니다");
        assertThat(user.isAlertAgreed()).isTrue();
    }

    @Test
    @DisplayName("/notify off → 알림이 꺼진다")
    void notify_off() {
        // given
        UserEntity user = UserEntity.builder()
                .slackId("U123")
                .isAlertAgreed(true)
                .build();

        when(userRepository.findBySlackId("U123"))
                .thenReturn(Optional.of(user));

        // when
        String result = slackCommandService.handleNotify("off", "U123");

        // then
        assertThat(result).contains("꺼졌습니다");
        assertThat(user.isAlertAgreed()).isFalse();
    }

    @Test
    @DisplayName("/notify status → 현재 상태를 반환한다")
    void notify_status() {
        // given
        UserEntity user = UserEntity.builder()
                .slackId("U123")
                .isAlertAgreed(true)
                .build();

        when(userRepository.findBySlackId("U123"))
                .thenReturn(Optional.of(user));

        // when
        String result = slackCommandService.handleNotify("status", "U123");

        // then
        assertThat(result).contains("ON");
    }

    @Test
    @DisplayName("text가 null이면 help 메시지를 반환한다")
    void notify_help_when_text_null() {
        // given
        UserEntity user = UserEntity.builder()
                .slackId("U123")
                .build();

        when(userRepository.findBySlackId("U123"))
                .thenReturn(Optional.of(user));

        // when
        String result = slackCommandService.handleNotify(null, "U123");

        // then
        assertThat(result).contains("사용법");
    }

    @Test
    @DisplayName("SlackId에 해당하는 유저가 없으면 예외 발생")
    void user_not_found() {
        // given
        when(userRepository.findBySlackId("U999"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                slackCommandService.handleNotify("on", "U999")
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
