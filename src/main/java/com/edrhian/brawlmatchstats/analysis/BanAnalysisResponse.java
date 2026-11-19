package com.edrhian.brawlmatchstats.analysis;

import java.util.List;

/**
 * Response of the ban analysis.
 *
 * - battles: how many battles passed the filters
 * - battlesWithBans: how many of them have at least one ban (the banrate is calculated over these)
 * - confidence: how much to trust the banrates, based on battlesWithBans
 * - bans: the banned brawlers, the most banned first
 */
public record BanAnalysisResponse(
        int battles,
        int battlesWithBans,
        Confidence confidence,
        List<BanStatsDto> bans
) {
}