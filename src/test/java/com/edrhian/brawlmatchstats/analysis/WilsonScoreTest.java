package com.edrhian.brawlmatchstats.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class WilsonScoreTest {

    @Test
    void intervalForEightWinsOutOfTen() {
        WilsonScore.Interval interval = WilsonScore.interval(8, 10);

        assertThat(interval.lower()).isCloseTo(0.4902, within(0.001));
        assertThat(interval.upper()).isCloseTo(0.9433, within(0.001));
    }

    @Test
    void intervalForFiftyWinsOutOfHundred() {
        WilsonScore.Interval interval = WilsonScore.interval(50, 100);

        assertThat(interval.lower()).isCloseTo(0.4038, within(0.001));
        assertThat(interval.upper()).isCloseTo(0.5962, within(0.001));
    }

    @Test
    void intervalForZeroWins() {
        WilsonScore.Interval interval = WilsonScore.interval(0, 10);

        assertThat(interval.lower()).isZero();
        assertThat(interval.upper()).isCloseTo(0.2775, within(0.001));
    }

    @Test
    void intervalWithoutGamesIsZero() {
        WilsonScore.Interval interval = WilsonScore.interval(0, 0);

        assertThat(interval.lower()).isZero();
        assertThat(interval.upper()).isZero();
    }

    @Test
    void statsRoundPercentagesAndAddConfidence() {
        StatsDto stats = StatsDto.of(8, 2, 0);

        assertThat(stats.games()).isEqualTo(10);
        assertThat(stats.winrate()).isEqualTo(80.0);
        assertThat(stats.wilsonLower()).isEqualTo(49.0);
        assertThat(stats.wilsonUpper()).isEqualTo(94.3);
        assertThat(stats.confidence()).isEqualTo(Confidence.LOW);
    }

    @Test
    void statsCountDrawsAsGamesButNotWins() {
        StatsDto stats = StatsDto.of(1, 1, 2);

        assertThat(stats.games()).isEqualTo(4);
        assertThat(stats.winrate()).isEqualTo(25.0);
        assertThat(stats.draws()).isEqualTo(2);
    }

    @Test
    void confidenceDependsOnSampleSize() {
        assertThat(Confidence.of(0)).isEqualTo(Confidence.INSUFFICIENT);
        assertThat(Confidence.of(9)).isEqualTo(Confidence.INSUFFICIENT);
        assertThat(Confidence.of(10)).isEqualTo(Confidence.LOW);
        assertThat(Confidence.of(29)).isEqualTo(Confidence.LOW);
        assertThat(Confidence.of(30)).isEqualTo(Confidence.MEDIUM);
        assertThat(Confidence.of(99)).isEqualTo(Confidence.MEDIUM);
        assertThat(Confidence.of(100)).isEqualTo(Confidence.HIGH);
    }
}