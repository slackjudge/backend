package com.project.scheduler;

import com.project.service.RankChangeStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**==========================
*
* 순위 변동 알림 스케줄러를 실행한다.
*
* @parm -
* @return void
* @author 김경민
* @version 1.0.0
* @date 2025-12-14
*
==========================**/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class RankChangeScheduler {

    private final RankChangeStateService rankChangeStateService;

    @Scheduled(cron = "0 10 * * * *", zone = "Asia/Seoul")
    public void runRankChange() {
        log.info("[RankChangeScheduler] run");
        rankChangeStateService.sendRankChangeMessage();
    }
}
