package com.project.controller;

import com.project.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(NotificationControllerTest.TestConfig.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    NotificationService notificationService;

    static class TestConfig {
        @Bean
        public NotificationService notificationService() {
            return mock(NotificationService.class);
        }
    }

    @Test
    @DisplayName("알림 조회 API 첫 페이지 호출 성공")
    void getNotifications_firstPage_api_ok() throws Exception {
        when(notificationService.getNotifications(null, 20))
                .thenReturn(List.of());

        mockMvc.perform(get("/notification"))
                .andExpect(status().isOk());

        verify(notificationService).getNotifications(null, 20);
    }

    @Test
    @DisplayName("알림 조회 API 커서 기반 호출 성공")
    void getNotifications_withCursor_api_ok() throws Exception {
        when(notificationService.getNotifications(10L, 5))
                .thenReturn(List.of());

        mockMvc.perform(get("/notification")
                        .param("lastId", "10")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(notificationService).getNotifications(10L, 5);
    }
}
