package com.project.scheduler;

import com.project.service.RankChangeStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RankChangeSchedulerTest {

    @InjectMocks
    RankChangeScheduler rankChangeScheduler;

    @Mock
    RankChangeStateService rankChangeStateService;

    @Test
    void dailyRankScheduler_runs() {
        rankChangeScheduler.runRankChange();
        verify(rankChangeStateService).sendRankChangeMessage();
    }
}
