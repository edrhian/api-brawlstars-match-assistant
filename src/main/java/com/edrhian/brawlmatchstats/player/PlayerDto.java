package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.shared.BattleType;

import java.util.List;

/**
 * The player data we send and receive through the API.
 * It has the same fields as the Player entity, but it is not tied to the database.
 */
public record PlayerDto(
        String tag,
        String name,
        Integer trophies,
        String rankedRank,
        boolean trackingEnabled,
        List<BattleType> trackingTypes,
        String lastUpdated
) {
}