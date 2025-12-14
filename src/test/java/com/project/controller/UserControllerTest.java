package com.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.security.SecurityUserDetails;
import com.project.common.security.jwt.access.AccessTokenProvider;
import com.project.common.security.jwt.refresh.RefreshTokenProvider;
import com.project.common.util.BojUtil;
import com.project.common.util.SlackUtil;
import com.project.dto.request.SignUpRequest;
import com.project.entity.EurekaTeamName;
import com.project.entity.UserEntity;
import com.project.repository.RefreshTokenRepository;
import com.project.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}