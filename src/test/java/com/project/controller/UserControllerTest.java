package com.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.security.SecurityUserDetails;
import com.project.common.security.jwt.access.AccessTokenProvider;
import com.project.common.security.jwt.refresh.RefreshTokenProvider;
import com.project.common.util.BojUtil;
import com.project.common.util.SlackUtil;
import com.project.dto.request.SignUpRequest;
import com.project.dto.response.GrassResponse;
import com.project.dto.response.MyPageDetailResponse;
import com.project.dto.response.MyPageResponse;
import com.project.dto.response.ProblemResponse;
import com.project.dto.response.MyPageProfileResponse;
import com.project.entity.EurekaTeamName;
import com.project.entity.UserEntity;
import com.project.repository.RefreshTokenRepository;
import com.project.repository.UserRepository;
import com.project.service.MyPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@SqlGroup({
        @Sql(value = "/sql/insert-test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-test-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@Transactional
class UserControllerTest {


    @MockitoBean
    AccessTokenProvider accessTokenProvider;
    @MockitoBean
    RefreshTokenProvider refreshTokenProvider;
    @MockitoBean
    SlackUtil slackUtil;
    @MockitoBean
    RefreshTokenRepository refreshTokenRepository;
    @MockitoBean
    BojUtil bojUtil;
    @MockitoBean
    MyPageService myPageService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setupSecurityContext() {
        UserEntity user = userRepository.findById(1L).orElseThrow();
        SecurityUserDetails userDetails = new SecurityUserDetails(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("BOJ ID 검증 테스트")
    void checkUser() throws Exception {

        // BOJ MOCK
        when(bojUtil.checkBojId("dlrbehd120")).thenReturn(true);

        // when & then
        mockMvc.perform(get("/user/check")
                        .param("baekjoonId", "dlrbehd120"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("백준 회원 체크 성공"))
                .andExpect(jsonPath("$.data.baekjoonId").value("dlrbehd120"))
                .andExpect(jsonPath("$.data.isBaekjoonId").value(true));
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp() throws Exception {

        // BOJ 티어 Mock
        when(bojUtil.getBojTier("dlrbehd120")).thenReturn(14);

        SignUpRequest request = new SignUpRequest(
                "이규동",
                "dlrbehd120",
                EurekaTeamName.BACKEND_FACE,
                true
        );

        // WHEN & THEN
        mockMvc.perform(post("/user/signUp")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        UserEntity updatedUser = userRepository.findById(1L).get();

        assertThat(updatedUser.getUserId()).isEqualTo(1L);
        assertThat(updatedUser.getBaekjoonId()).isEqualTo("dlrbehd120");
        assertThat(updatedUser.getUsername()).isEqualTo("이규동");
        assertThat(updatedUser.getTeamName()).isEqualTo(EurekaTeamName.BACKEND_FACE);
        assertThat(updatedUser.getBojTier()).isEqualTo(14);
    }
    @Test
    @DisplayName("마이페이지 조회 성공 테스트")
    void getMyPage() throws Exception {
        // Given
        int year = 2025;
        int month = 12;
        String dateStr = "2025-12-05";
        Long userId = 1L;

        // 1. Mock 데이터 생성 (Record 생성자 순서 및 타입 주의)
        MyPageProfileResponse profileMock = new MyPageProfileResponse("이규동", "dlrbehd120", 14,   // tierLevel (bojTier 아님)
                100L  // totalScore
        );

        // GrassResponse (로그상 키값: date, solvedCount)
        List<GrassResponse> grassMock = List.of(new GrassResponse("2025-12-05", 2));

        // MyPageDetailResponse (로그상 키값: date, dailyScore, dailyRank, solvedCount, maxDifficulty, problems)
        MyPageDetailResponse detailMock = new MyPageDetailResponse(dateStr, 15, // dailyScore
                1,  // dailyRank
                2,  // solvedCount
                10, // maxDifficulty
                List.of(new ProblemResponse("A+B", 1, "url")));

        // MyPageResponse 조립
        MyPageResponse mockResponse = new MyPageResponse(profileMock, grassMock, detailMock);

        // Service Mocking
        // (파라미터 매칭을 위해 any() 사용하거나 정확한 값 입력)
        when(myPageService.getMyPage(any(), anyInt(), anyInt(), any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/user/me").param("year", String.valueOf(year)).param("month", String.valueOf(month)).param("date", dateStr)).andExpect(status().isOk()).andDo(print()) // 성공 시 로그 출력

                // 1. 공통 응답 확인
                .andExpect(jsonPath("$.message").value("마이페이지 조회 성공"))

                // 2. Profile 검증 (Record 필드명: tierLevel)
                .andExpect(jsonPath("$.data.profile.username").value("이규동")).andExpect(jsonPath("$.data.profile.baekjoonId").value("dlrbehd120")).andExpect(jsonPath("$.data.profile.tierLevel").value(14))

                // 3. Grass 검증 (로그상 키값: solvedCount)
                .andExpect(jsonPath("$.data.grass[0].date").value("2025-12-05")).andExpect(jsonPath("$.data.grass[0].solvedCount").value(2)) // count -> solvedCount 수정

                // 4. Detail 검증 (★중요: detail -> selectedDateDetail)
                // Record 내부 변수명이 selectedDateDetail이라서 JSON 키도 그렇게 나옵니다.
                .andExpect(jsonPath("$.data.selectedDateDetail.date").value("2025-12-05")).andExpect(jsonPath("$.data.selectedDateDetail.dailyScore").value(15)).andExpect(jsonPath("$.data.selectedDateDetail.solvedCount").value(2));
    }
}