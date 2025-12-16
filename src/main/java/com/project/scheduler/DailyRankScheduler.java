package com.project.scheduler;

import com.project.service.DailyRankMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class DailyRankScheduler {

    private final DailyRankMessageService dailyRankMessageService;

    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void runDailyRank() {
        log.info("[DailyRankScheduler] run");
        dailyRankMessageService.sendDailyRankMessage();
    }
}
