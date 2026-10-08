package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;

import java.util.List;

/**
 * The values that exist in the database for some players, to build the filter selectors.
 * firstBattle and lastBattle use the format 2026-10-01T10:00:00 (UTC).
 */
public record FilterOptionsResponse(
        List<BattleType> battleTypes,
        List<String> ranks,
        List<String> gameModes,
        List<String> maps,
        Integer trophiesMin,
        Integer trophiesMax,
        String firstBattle,
        String lastBattle
) {
}