package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;

    public void sendDailyRankMessage() {
        try {
            String rank1 = "유재석";
            int solved1 = 7;
            int score1 = 48;

            String rank2 = "정형돈";
            int solved2 = 5;
            int score2 = 32;

            String rank3 = "노홍철";
            int solved3 = 4;
            int score3 = 30;

            String message = messageFormatUtil.formatDailyRank(rank1, solved1, score1, rank2, solved2, score2, rank3, solved3, score3);
            slackMessageSender.sendMessage("C0A0M8HUQDT", message);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SLACK_MESSAGE_FAILED, "slack 메시지 전송 중 오류 발생 : " + e.getMessage());
        }
    }

    public void sendRankChangeMessage() {
        try {
            String userId = "U0A1NG7GEA2";

            String userName = "박명수";
            int oldRank = 5;
            int newRank = 3;
            int score = 26;

            String message = messageFormatUtil.formatRankChange(userName, oldRank, newRank, score);

            slackMessageSender.sendMessage(userId, message);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SLACK_MESSAGE_FAILED, "slack 메시지 전송 중 오류 발생 : " + e.getMessage());
        }
    }
}
