package com.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.BojUtil;
import com.project.dto.request.SignUpRequest;
import com.project.dto.response.BojCheckResponse;
import com.project.entity.EurekaTeamName;
import com.project.entity.UserEntity;
import com.project.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private BojUtil bojUtil;

  @InjectMocks private UserService userService;

  @Test
  @DisplayName("존재하는 유저 조회 성공")
  void findUserSuccess() {

    // given
    Long userId = 1L;
    UserEntity user = UserEntity.createUser("SLACK123");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    UserEntity result = userService.findUser(userId);

    // then
    assertThat(result).isSameAs(user);
    verify(userRepository).findById(userId);
  }

  @Test
  @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외 발생")
  void findUserError() {

    // given
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.findUser(userId))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("소셜 로그인 후 해당 유저가 존재하지 않으면 새로 생성해서 저장 후 반환")
  void findBySlackIdSuccess() {

    // given
    String slackId = "SLACK12345";
    when(userRepository.findBySlackId(slackId)).thenReturn(Optional.empty());

    UserEntity savedUser = UserEntity.createUser(slackId);
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    // when
    UserEntity result = userService.findUserBySlackId(slackId);

    // then
    assertThat(result).isSameAs(savedUser);
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("회원가입시 BOJ 티어 조회 후 유저 정보 업데이트")
  void signUpSuccess() {
    // given
    Long userId = 1L;
    UserEntity user = UserEntity.createUser("SLACK123");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    SignUpRequest request =
        new SignUpRequest("규동", "dlrbehd120", EurekaTeamName.BACKEND_FACE, true);

    int tier = 14;
    when(bojUtil.getBojTier(request.baekjoonId())).thenReturn(tier);

    // when
    userService.signUp(userId, request);

    // then
    assertThat(user.getUsername()).isEqualTo(request.username());
    assertThat(user.getBaekjoonId()).isEqualTo(request.baekjoonId());
    assertThat(user.getTeamName()).isEqualTo(request.teamName());
    assertThat(user.getBojTier()).isEqualTo(14);

    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("사용자 백준 아이디 검증 성공")
  void checkBojSuccess() {
    // given
    Long userId = 1L;
    String bojId = "dlrbehd120";

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(UserEntity.createUser("SLACK123")));
    when(bojUtil.checkBojId(bojId)).thenReturn(true);

    // when
    BojCheckResponse response = userService.checkBaekjoonId(userId, bojId);

    // then
    assertThat(response.baekjoonId()).isEqualTo(bojId);
    assertThat(response.isBaekjoonId()).isTrue();
  }

  @Test
  @DisplayName("사용자 백준 아이디 검증 실패")
  void checkBojError() {
    // given
    Long userId = 1L;
    String bojId = "abcdefghi";

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(UserEntity.createUser("SLACK123")));
    when(bojUtil.checkBojId(bojId)).thenReturn(false);

    // when
    BojCheckResponse response = userService.checkBaekjoonId(userId, bojId);

    // then
    assertThat(response.baekjoonId()).isEqualTo(bojId);
    assertThat(response.isBaekjoonId()).isFalse();
  }
}
