package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.entity.UserEntity;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 김경민
 */
@Service
@RequiredArgsConstructor
public class SlackCommandService {

    private final UserRepository userRepository;

    /**==========================
    *
    * Slack 알림 관련 명령을 처리한다.
    *
    * @parm text 명령어 텍스트
    * @parm slackId Slack 사용자 ID
    * @return String 처리 결과 메시지
    * @author 김경민
    * @version 1.0.0
    * @date 2025-12-16
    *
    ==========================**/
    @Transactional
    public String handleNotify(String text, String slackId) {
        UserEntity user = userRepository.findBySlackId(slackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String command = normalize(text);

        return switch (command) {
            case "on" -> {
                user.updateAlertAgreed(true);
                yield "🔔 순위 변동 알림이 켜졌습니다.";
            }
            case "off" -> {
                user.updateAlertAgreed(false);
                yield "🔕 순위 변동 알림이 꺼졌습니다.";
            }
            case "status" -> user.isAlertAgreed()
                    ? "🔔 현재 알림 상태: ON"
                    : "🔕 현재 알림 상태: OFF";
            default -> helpMessage();
        };
    }

    private String normalize(String text) {
        if (text == null || text.isBlank()) return "help";
        return text.trim().toLowerCase();
    }

    private String helpMessage() {
        return """
        ❓ 사용법 안내

        /notify on     알림 켜기
        /notify off    알림 끄기
        /notify status 상태 확인
        """;
    }
}
