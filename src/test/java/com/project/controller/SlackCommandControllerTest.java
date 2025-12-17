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

/**
 * @author 김경민
 */
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
    @DisplayName("/notify on → 알림 켜기 응답")
    void notify_on() throws Exception {
        when(slackCommandService.handleNotify("on", "U123"))
                .thenReturn("ON MESSAGE");

        performAndExpect("on", "ON MESSAGE");
    }

    @Test
    @DisplayName("/notify off → 알림 끄기 응답")
    void notify_off() throws Exception {
        when(slackCommandService.handleNotify("off", "U123"))
                .thenReturn("OFF MESSAGE");

        performAndExpect("off", "OFF MESSAGE");
    }

    @Test
    @DisplayName("/notify status → 상태 조회 응답")
    void notify_status() throws Exception {
        when(slackCommandService.handleNotify("status", "U123"))
                .thenReturn("STATUS MESSAGE");

        performAndExpect("status", "STATUS MESSAGE");
    }

    @Test
    @DisplayName("/notify invalid → help 메시지 응답")
    void notify_invalid() throws Exception {
        when(slackCommandService.handleNotify("invalid", "U123"))
                .thenReturn("HELP MESSAGE");

        performAndExpect("invalid", "HELP MESSAGE");
    }

    @Test
    @DisplayName("/notify without text → help 메시지 응답")
    void notify_empty_text() throws Exception {
        when(slackCommandService.handleNotify(null, "U123"))
                .thenReturn("HELP MESSAGE");

        mockMvc.perform(
                        post("/slack/command/notify")
                                .contentType("application/x-www-form-urlencoded")
                                .param("user_id", "U123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_type").value("ephemeral"))
                .andExpect(jsonPath("$.text").value("HELP MESSAGE"));
    }

    private void performAndExpect(String text, String expectedMessage) throws Exception {
        mockMvc.perform(
                        post("/slack/command/notify")
                                .contentType("application/x-www-form-urlencoded")
                                .param("text", text)
                                .param("user_id", "U123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_type").value("ephemeral"))
                .andExpect(jsonPath("$.text").value(expectedMessage));
    }
}
