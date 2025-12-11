package com.project.controller;

import com.project.config.security.JpaAuditingConfig;
import com.project.service.SlackNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlackNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
class SlackNotificationControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    SlackNotificationService slackNotificationService;

    @Test
    @DisplayName("일일 랭킹 알림 API 호출 성공")
    void dailyRankApiTest() throws Exception {

        mvc.perform(post("/slack/daily-rank"))
                .andExpect(status().isOk());

        verify(slackNotificationService, times(1)).sendDailyRankMessage();
    }
}
