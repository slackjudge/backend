package com.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.security.SecurityUserDetails;
import com.project.common.security.jwt.access.AccessTokenProvider;
import com.project.common.security.jwt.refresh.RefreshTokenProvider;
import com.project.common.util.BojUtil;
import com.project.common.util.SlackUtil;
import com.project.dto.request.SignUpRequest;
import com.project.dto.response.MyPageResponse;
import com.project.entity.EurekaTeamName;
import com.project.entity.UserEntity;
import com.project.repository.RefreshTokenRepository;
import com.project.repository.UserRepository;
import com.project.service.MyPageService;
import java.util.Collections;
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@SqlGroup({
  @Sql(value = "/sql/insert-test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
  @Sql(value = "/sql/delete-test-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@Transactional
class UserControllerTest {

  @MockitoBean AccessTokenProvider accessTokenProvider;
  @MockitoBean RefreshTokenProvider refreshTokenProvider;
  @MockitoBean SlackUtil slackUtil;
  @MockitoBean RefreshTokenRepository refreshTokenRepository;
  @MockitoBean BojUtil bojUtil;

  @MockitoBean MyPageService myPageService;

  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  void setupSecurityContext() {
    UserEntity user = userRepository.findById(1L).orElseThrow();
    SecurityUserDetails userDetails = new SecurityUserDetails(user);

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  @DisplayName("BOJ ID 검증 테스트")
  void checkUser() throws Exception {

    // BOJ MOCK
    when(bojUtil.checkBojId("dlrbehd120")).thenReturn(true);

    // when & then
    mockMvc
        .perform(get("/user/check").param("baekjoonId", "dlrbehd120"))
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

    SignUpRequest request =
        new SignUpRequest("이규동", "dlrbehd120", EurekaTeamName.BACKEND_FACE, true);

    // WHEN & THEN
    mockMvc
        .perform(
            post("/user/signUp")
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
    // given
    int year = 2025;
    int month = 12;
    String date = "2025-12-11";

    // 가짜 데이터 생성
    MyPageResponse mockResponse =
        MyPageResponse.builder()
            .profile(
                MyPageResponse.Profile.builder()
                    .username("이규동")
                    .baekjoonId("dlrbehd120")
                    .tierLevel(14)
                    .totalScore(1000L)
                    .build())
            .grass(Collections.emptyList())
            .selectedDateDetail(
                MyPageResponse.SelectedDateDetail.builder()
                    .date(date)
                    .dailyScore(20)
                    .dailyRank(1)
                    .solvedCount(2)
                    .maxDifficulty(10)
                    .problems(Collections.emptyList())
                    .build())
            .build();
    // when: service 메서드 호출시
    when(myPageService.getMyPageData(anyLong(), eq(year), eq(month), eq(date)))
        .thenReturn(mockResponse);

    // then : API 호출 및 검증
    mockMvc
        .perform(
            get("/user/me")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .param("date", date))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("마이페이지 조회 성공"))
        // Profile 검증
        .andExpect(jsonPath("$.data.profile.username").value("이규동"))
        .andExpect(jsonPath("$.data.profile.baekjoonId").value("dlrbehd120"))
        // SelectedDateDetail 검증
        .andExpect(jsonPath("$.data.selectedDateDetail.date").value(date))
        .andExpect(jsonPath("$.data.selectedDateDetail.dailyScore").value(20));
  }
}
