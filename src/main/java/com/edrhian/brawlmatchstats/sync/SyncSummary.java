package com.edrhian.brawlmatchstats.sync;

/**
 * Result of syncing all the tracked players.
 * stoppedEarly is true when the API rejected us (bad key, rate limit, maintenance),
 * so the rest of the players were not tried.
 */
public record SyncSummary(
        int trackedPlayers,
        int syncedPlayers,
        int failedPlayers,
        int newBattles,
        boolean stoppedEarly
) {
}