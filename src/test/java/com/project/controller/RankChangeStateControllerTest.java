package com.project.controller;

import com.project.service.RankChangeStateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankChangeStateController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RankChangeStateControllerTest.TestConfig.class)
public class RankChangeStateControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    RankChangeStateService rankChangeStateService;

    static class TestConfig {
        @Bean
        public RankChangeStateService rankChangeStateService() {
            return mock(RankChangeStateService.class);
        }
    }
    @Test
    @DisplayName("개인 순위 변동 알림 API 호출 성공")
    void rankChangeApiTest() throws Exception {

        mvc.perform(post("/slack/rank-change"))
                .andExpect(status().isOk());

        verify(rankChangeStateService, times(1)).sendRankChangeMessage();
    }
}
