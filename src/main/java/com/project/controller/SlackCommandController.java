package com.project.controller;

import com.project.common.dto.SlackCommandResponse;
import com.project.service.SlackCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 김경민
 */
@RestController
@RequestMapping("/slack/command")
@RequiredArgsConstructor
public class SlackCommandController {

    private final SlackCommandService slackCommandService;

    /**==========================
    *
    * Slack notify 명령을 처리한다.
    *
    * @parm body Slack 요청 바디
    * @return ResponseEntity<SlackCommandResponse>
    * @author 김경민
    * @version 1.0.0
    * @date 2025-12-16
    *
    ==========================**/
    @PostMapping("/notify")
    public ResponseEntity<SlackCommandResponse> notify(@RequestBody MultiValueMap<String, String> body) {
        String text = body.getFirst("text");      // on | off | status
        String slackId = body.getFirst("user_id");

        String message = slackCommandService.handleNotify(text, slackId);

        return ResponseEntity.ok(SlackCommandResponse.ephemeral(message));
    }
}
