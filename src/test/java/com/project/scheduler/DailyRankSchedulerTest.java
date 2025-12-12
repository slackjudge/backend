package com.project.scheduler;

import com.project.service.SlackNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DailyRankSchedulerTest {

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
