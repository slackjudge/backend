package com.project.scheduler;

import com.project.service.SlackNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

public class DailyRankSchedulerTest {

    @InjectMocks
    DailyRankScheduler dailyRankScheduler;

    @Mock
    SlackNotificationService slackNotificationService;

    @Test
    void dailyRankScheduler_runs() {
        dailyRankScheduler.runDailyRank();
        verify(slackNotificationService).sendDailyRankMessage();
    }
}
