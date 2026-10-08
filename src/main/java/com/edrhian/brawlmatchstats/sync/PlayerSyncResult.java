package com.edrhian.brawlmatchstats.sync;

import com.edrhian.brawlmatchstats.player.PlayerDto;

/**
 * Result of syncing one player: the refreshed profile and how many new battles were stored.
 */
public record PlayerSyncResult(PlayerDto player, int newBattles) {
}