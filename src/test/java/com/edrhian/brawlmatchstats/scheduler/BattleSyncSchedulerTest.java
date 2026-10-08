package com.edrhian.brawlmatchstats.scheduler;

import com.edrhian.brawlmatchstats.sync.SyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BattleSyncSchedulerTest {

    @Mock
    private SyncService syncService;

    @Test
    void runsTheSyncOfAllTrackedPlayers() {
        new BattleSyncScheduler(syncService).syncTrackedPlayers();

        verify(syncService).syncTrackedPlayers();
    }
}