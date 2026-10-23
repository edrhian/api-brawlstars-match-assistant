package com.edrhian.brawlmatchstats.analysis;

/**
 * The same numbers as StatsDto, for one Brawler.
 */
public record BrawlerStatsDto(
        String brawler,
        int games,
        int wins,
        int losses,
        int draws,
        double winrate,
        double wilsonLower,
        double wilsonUpper,
        Confidence confidence
) {

    public static BrawlerStatsDto of(String brawler, int wins, int losses, int draws) {
        StatsDto stats = StatsDto.of(wins, losses, draws);
        return new BrawlerStatsDto(brawler, stats.games(), stats.wins(), stats.losses(), stats.draws(),
                stats.winrate(), stats.wilsonLower(), stats.wilsonUpper(), stats.confidence());
    }
}