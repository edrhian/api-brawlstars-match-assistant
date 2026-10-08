package com.edrhian.brawlmatchstats.battle;

/**
 * Lowest and highest trophies and battle times of a group of battles.
 * Everything is null when there are no battles.
 */
public record BattleRange(
        Integer minTrophies,
        Integer maxTrophies,
        String firstBattleTime,
        String lastBattleTime
) {
}