package com.project.scheduler;

import com.project.service.RankChangeStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankChangeScheduler {

    private final RankChangeStateService rankChangeStateService;

    @Scheduled(cron = "0 15 * * * *", zone = "Asia/Seoul")
    public void runRankChange() {
        log.info("[RankChangeScheduler] run");
        rankChangeStateService.sendRankChangeMessage();
    }
}
