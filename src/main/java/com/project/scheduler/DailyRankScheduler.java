package com.project.scheduler;

import com.project.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyRankScheduler {

    private final SlackNotificationService slackNotificationService;

    @Scheduled(cron = "0 45 17 * * *", zone = "Asia/Seoul")
    public void runDailyRank() {
        slackNotificationService.sendDailyRankMessage();
    }
}
