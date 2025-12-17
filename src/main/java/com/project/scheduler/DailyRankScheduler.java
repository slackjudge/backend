package com.project.scheduler;

import com.project.service.DailyRankMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**==========================
*
* 일일 랭킹 메시지 전송 스케줄러를 실행한다.
*
* @parm -
* @return void
* @author 김경민
* @version 1.0.0
* @date 2025-12-12
*
==========================**/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class DailyRankScheduler {

    private final DailyRankMessageService dailyRankMessageService;

    @Scheduled(cron = "0 0 9,15,21 * * *", zone = "Asia/Seoul")
    public void runDailyRank() {
        log.info("[DailyRankScheduler] run");
        dailyRankMessageService.sendDailyRankMessage();
    }
}
