package com.project.controller;

import com.project.service.SlackNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlackNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SlackNotificationControllerTest.TestConfig.class)
class SlackNotificationControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SlackNotificationService slackNotificationService;

    static class TestConfig {
        @Bean
        public SlackNotificationService slackNotificationService() {
            return mock(SlackNotificationService.class);
        }
    }


    @Test
    @DisplayName("일일 랭킹 알림 API 호출 성공")
    void dailyRankApiTest() throws Exception {

        mvc.perform(post("/slack/daily-rank"))
                .andExpect(status().isOk());

        verify(slackNotificationService, times(1)).sendDailyRankMessage();
    }

    @Test
    @DisplayName("개인 순위 변동 알림 API 호출 성공")
    void rankChangeApiTest() throws Exception {

        mvc.perform(post("/slack/rank-change"))
                .andExpect(status().isOk());

        verify(slackNotificationService, times(1)).sendRankChangeMessage();
    }
}
