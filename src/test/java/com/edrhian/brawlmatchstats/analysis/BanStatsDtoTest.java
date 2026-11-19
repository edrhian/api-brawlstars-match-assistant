package com.edrhian.brawlmatchstats.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BanStatsDtoTest {

    @Test
    void calculatesTheBanrateAndTheWilsonInterval() {
        // Banned in 4 of 5 battles with bans: 80%, and with so few battles the range is very wide
        BanStatsDto stats = BanStatsDto.of("COLT", 6, 4, 5);

        assertThat(stats.brawler()).isEqualTo("COLT");
        assertThat(stats.bans()).isEqualTo(6);
        assertThat(stats.bannedBattles()).isEqualTo(4);
        assertThat(stats.banrate()).isEqualTo(80.0);
        assertThat(stats.wilsonLower()).isEqualTo(37.6);
        assertThat(stats.wilsonUpper()).isEqualTo(96.4);
    }

    @Test
    void banrateIsZeroWithoutBattles() {
        BanStatsDto stats = BanStatsDto.of("COLT", 0, 0, 0);

        assertThat(stats.banrate()).isZero();
        assertThat(stats.wilsonLower()).isZero();
        assertThat(stats.wilsonUpper()).isZero();
    }
}