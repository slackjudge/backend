package com.project.controller;

import com.project.service.SlackCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlackCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SlackCommandControllerTest.TestConfig.class)
class SlackCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SlackCommandService slackCommandService;

    static class TestConfig {
        @Bean
        public SlackCommandService slackCommandService() {
            return mock(SlackCommandService.class);
        }
    }

    @Test
    @DisplayName("Slack /notify 요청 시 ephemeral 응답을 반환한다")
    void notify_command() throws Exception {
        // given
        when(slackCommandService.handleNotify("on", "U123"))
                .thenReturn("🔔 순위 변동 알림이 켜졌습니다.");

        // when & then
        mockMvc.perform(
                        post("/slack/command/notify")
                                .contentType("application/x-www-form-urlencoded")
                                .param("text", "on")
                                .param("user_id", "U123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_type").value("ephemeral"))
                .andExpect(jsonPath("$.text").value("🔔 순위 변동 알림이 켜졌습니다."));
    }
}
