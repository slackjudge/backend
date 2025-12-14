package com.project.entity;

import com.project.dto.request.LocalSignRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("localSignUp으로 UserEntity 객체를 생성할 수 있다")
    void localSignUp_updateFields() {

        // given (기본 유저 생성)
        UserEntity user = UserEntity.createUser("initial-slackId");

        LocalSignRequest request = new LocalSignRequest(
                "홍길동",
                "gildong123",
                EurekaTeamName.BACKEND_FACE,
                true
        );
        int bojTier = 15;

        // when
        user.localSignUp(request, bojTier);

        // then
        assertThat(user.getSlackId()).isEqualTo("local");
        assertThat(user.getUsername()).isEqualTo("홍길동");
        assertThat(user.getBaekjoonId()).isEqualTo("gildong123");
        assertThat(user.getTeamName()).isEqualTo(EurekaTeamName.BACKEND_FACE);
        assertThat(user.isAlertAgreed()).isTrue();
        assertThat(user.getBojTier()).isEqualTo(15);
    }
}