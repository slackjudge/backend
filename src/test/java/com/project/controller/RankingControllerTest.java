package com.project.controller;


import com.project.dto.response.RankingPageResponse;
import com.project.dto.response.RankingRowResponse;
import com.project.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * author : 박준희
 */
@WebMvcTest(RankingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RankingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RankingService rankingService;

    @Test
    @DisplayName("쿼리 파라미터 없으면 기본값(period=day, group=ALL, page=1, size=20)과 null dateTime이 서비스에 전달된다")
    void getRanking_usesDefaultParams_whenNoQueryParams() throws Exception {
        // given
        RankingPageResponse dummyResponse =
                new RankingPageResponse(false, Collections.emptyList());

        given(rankingService.getRanking(anyString(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(dummyResponse);

        // when & then
        mockMvc.perform(get("/rank")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(rankingService).getRanking(
                eq("day"),
                isNull(),
                eq("ALL"),
                eq(1),
                eq(20)
        );
    }

    @Test
    @DisplayName("쿼리 파라미터가 전달되면 그대로 서비스로 위임된다")
    void getRanking_passesAllQueryParamsToService() throws Exception {
        // given
        String period = "week";
        String group = "BACKEND_NON_FACE";
        int page = 2;
        int size = 50;
        String dateTimeStr = "2025-12-11T14:30:00";
        LocalDateTime expectedDateTime = LocalDateTime.parse(dateTimeStr);

        RankingPageResponse dummyResponse =
                new RankingPageResponse(true, Collections.singletonList(
                        new RankingRowResponse(
                                1L,
                                1,
                                15,
                                "홍길동",
                                100,
                                10L,
                                "baekjoon123",
                                "BACKEND_NON_FACE",
                                0
                        )
                ));

        given(rankingService.getRanking(anyString(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(dummyResponse);

        // when & then
        mockMvc.perform(get("/rank")
                        .param("period", period)
                        .param("dateTime", dateTimeStr)
                        .param("group", group)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(rankingService).getRanking(
                eq(period),
                eq(expectedDateTime),
                eq(group),
                eq(page),
                eq(size)
        );
    }
}
