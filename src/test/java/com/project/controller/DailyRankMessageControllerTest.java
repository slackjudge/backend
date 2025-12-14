package com.project.controller;

import com.project.service.DailyRankMessageService;
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

@WebMvcTest(DailyRankMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DailyRankMessageControllerTest.TestConfig.class)
class DailyRankMessageControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    DailyRankMessageService dailyRankMessageService;

    static class TestConfig {
        @Bean
        public DailyRankMessageService slackNotificationService() {
            return mock(DailyRankMessageService.class);
        }
    }


    @Test
    @DisplayName("일일 랭킹 알림 API 호출 성공")
    void dailyRankApiTest() throws Exception {

        mvc.perform(post("/slack/daily-rank"))
                .andExpect(status().isOk());

        verify(dailyRankMessageService, times(1)).sendDailyRankMessage();
    }

    @Test
    @DisplayName("개인 순위 변동 알림 API 호출 성공")
    void rankChangeApiTest() throws Exception {

        mvc.perform(post("/slack/rank-change"))
                .andExpect(status().isOk());

        verify(dailyRankMessageService, times(1)).sendRankChangeMessage();
    }
}
