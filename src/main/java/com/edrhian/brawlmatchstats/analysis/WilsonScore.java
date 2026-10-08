package com.edrhian.brawlmatchstats.analysis;

/**
 * Wilson score interval at 95% confidence.
 *
 * It tells us the range where the real winrate probably is. With few games the range is very
 * wide (8 wins out of 10 is somewhere between 49% and 94%), with many games it gets narrow.
 */
public final class WilsonScore {

    private static final double Z = 1.96; // 95% confidence

    private WilsonScore() {
        // Only static methods, nobody needs to create this class
    }

    // Lower and upper limit, as a proportion between 0 and 1
    public record Interval(double lower, double upper) {
    }

    public static Interval interval(int successes, int total) {
        if (total <= 0) {
            return new Interval(0, 0);
        }

        double n = total;
        double proportion = successes / n;
        double z2 = Z * Z;

        double denominator = 1 + z2 / n;
        double center = (proportion + z2 / (2 * n)) / denominator;
        double margin = Z * Math.sqrt(proportion * (1 - proportion) / n + z2 / (4 * n * n)) / denominator;

        return new Interval(Math.max(0, center - margin), Math.min(1, center + margin));
    }
}