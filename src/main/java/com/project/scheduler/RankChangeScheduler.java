package com.project.scheduler;

import com.project.service.RankChangeStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class RankChangeScheduler {

    private final RankChangeStateService rankChangeStateService;

    @Scheduled(cron = "0 10,50 * * * *", zone = "Asia/Seoul")
    public void runRankChange() {
        log.info("[RankChangeScheduler] run");
        rankChangeStateService.sendRankChangeMessage();
    }
}
