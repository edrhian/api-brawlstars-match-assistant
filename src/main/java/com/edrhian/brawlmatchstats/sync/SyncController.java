package com.edrhian.brawlmatchstats.sync;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints to force a sync without waiting for the scheduled job.
 * They refresh the player profile and fetch the battles in the same call.
 */
@RestController
@CrossOrigin(origins = "*")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    // All the players with tracking enabled
    @PostMapping("/sync")
    public SyncSummary syncAll() {
        return syncService.syncTrackedPlayers();
    }

    // One player (it also works if their tracking is disabled, but then only the profile is refreshed)
    @PostMapping("/players/{tag}/sync")
    public PlayerSyncResult syncPlayer(@PathVariable String tag) {
        return syncService.syncPlayer(tag);
    }
}