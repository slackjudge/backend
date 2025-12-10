package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import com.project.dto.DailyRankInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;

    public void sendDailyRankMessage() {
        try {
            DailyRankInfo rank1 = new DailyRankInfo("유재석", 7, 48);
            DailyRankInfo rank2 = new DailyRankInfo("정형돈", 5, 32);
            DailyRankInfo rank3 = new DailyRankInfo("노홍철", 4, 30);

            String message = messageFormatUtil.formatDailyRank(rank1, rank2, rank3);
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
