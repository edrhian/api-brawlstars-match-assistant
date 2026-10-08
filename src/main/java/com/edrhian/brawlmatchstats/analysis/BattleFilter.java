package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;

import java.util.List;

/**
 * The filters, already checked and ready to use. Empty lists and null values mean "no filter".
 *
 * - players: normalized tags (like #2VU8PGCC), at least one
 * - fromTime / toTime: in the format stored in the database (like 20261001T100000.000Z)
 * - maps: if there are maps, gameModes is ignored
 * - limit: only the N most recent battles, applied after all the other filters
 */
public record BattleFilter(
        List<String> players,
        List<BattleType> battleTypes,
        List<String> ranks,
        Integer trophiesMin,
        Integer trophiesMax,
        List<String> gameModes,
        List<String> maps,
        String fromTime,
        String toTime,
        Integer limit
) {
}