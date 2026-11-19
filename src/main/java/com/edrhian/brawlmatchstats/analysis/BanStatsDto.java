package com.edrhian.brawlmatchstats.analysis;

/**
 * Bans of one brawler.
 *
 * - bans: how many individual bans (two players can ban the same brawler in the same battle)
 * - bannedBattles: in how many battles it was banned at least once
 * - banrate: bannedBattles / battles with ban data, as a percentage with one decimal
 * - wilsonLower, wilsonUpper: Wilson score interval at 95% confidence of the banrate, as percentages
 */
public record BanStatsDto(
        String brawler,
        int bans,
        int bannedBattles,
        double banrate,
        double wilsonLower,
        double wilsonUpper
) {

    public static BanStatsDto of(String brawler, int bans, int bannedBattles, int battlesWithBans) {
        WilsonScore.Interval interval = WilsonScore.interval(bannedBattles, battlesWithBans);

        return new BanStatsDto(
                brawler, bans, bannedBattles,
                battlesWithBans == 0 ? 0.0 : round1(bannedBattles * 100.0 / battlesWithBans),
                round1(interval.lower() * 100),
                round1(interval.upper() * 100));
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}