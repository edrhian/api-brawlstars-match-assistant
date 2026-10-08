package com.edrhian.brawlmatchstats.analysis;

/**
 * How much we can trust a winrate, based only on the number of games.
 */
public enum Confidence {
    INSUFFICIENT,   // less than 10 games: it is noise
    LOW,            // 10 to 29 games
    MEDIUM,         // 30 to 99 games
    HIGH;           // 100 games or more

    public static Confidence of(int games) {
        if (games < 10) {
            return INSUFFICIENT;
        }
        if (games < 30) {
            return LOW;
        }
        if (games < 100) {
            return MEDIUM;
        }
        return HIGH;
    }
}