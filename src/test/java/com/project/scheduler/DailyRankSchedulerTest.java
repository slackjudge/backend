package com.project.scheduler;

import static org.mockito.Mockito.verify;

import com.project.service.SlackNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyRankSchedulerTest {

  @InjectMocks DailyRankScheduler dailyRankScheduler;

  @Mock SlackNotificationService slackNotificationService;

  @Test
  void dailyRankScheduler_runs() {
    dailyRankScheduler.runDailyRank();
    verify(slackNotificationService).sendDailyRankMessage();
  }
}
