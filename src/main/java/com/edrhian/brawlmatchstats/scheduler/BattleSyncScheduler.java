package com.edrhian.brawlmatchstats.scheduler;

import com.edrhian.brawlmatchstats.sync.SyncService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Every X minutes, syncs all the tracked players (profile and battles).
 * The work is done by SyncService, the same one used by the "force sync" endpoints.
 *
 * Set app.sync.enabled=false to turn it off.
 */
@Component
@ConditionalOnProperty(name = "app.sync.enabled", havingValue = "true", matchIfMissing = true)
public class BattleSyncScheduler {

    private final SyncService syncService;

    public BattleSyncScheduler(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * fixedDelay waits for the end of the previous run before counting the interval,
     * so two runs can never overlap. The first run starts when the app starts.
     */
    @Scheduled(fixedDelayString = "${app.sync.interval-minutes:15}", timeUnit = TimeUnit.MINUTES)
    public void syncTrackedPlayers() {
        syncService.syncTrackedPlayers();
    }
}