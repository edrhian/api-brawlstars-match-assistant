package com.edrhian.brawlmatchstats.analysis;

/**
 * Results of a group of battles, seen from the ally team.
 *
 * games = wins + losses + draws. A draw counts as a game but not as a win.
 * winrate, loserate and the Wilson limits are percentages (0 to 100) with one decimal.
 */
public record StatsDto(
        int games,
        int wins,
        int losses,
        int draws,
        double winrate,
        double loserate,
        double wilsonLower,
        double wilsonUpper,
        Confidence confidence
) {

    public static StatsDto of(int wins, int losses, int draws) {
        int games = wins + losses + draws;
        WilsonScore.Interval interval = WilsonScore.interval(wins, games);

        return new StatsDto(
                games, wins, losses, draws,
                percent(wins, games),
                percent(losses, games),
                round1(interval.lower() * 100),
                round1(interval.upper() * 100),
                Confidence.of(games));
    }

    private static double percent(int part, int games) {
        return games == 0 ? 0.0 : round1(part * 100.0 / games);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}