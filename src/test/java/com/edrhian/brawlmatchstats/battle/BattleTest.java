package com.edrhian.brawlmatchstats.battle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattleTest {

    private static final String TIME = "20261007T070930.000Z";

    @Test
    void idUsesTheSmallestTag() {
        String id = Battle.buildId(TIME, "#P0R9Y9J09", "#2VU8PGCC", "#9J8PRJRQL");

        assertThat(id).isEqualTo("20261007T070930.000Z_#2VU8PGCC");
    }

    @Test
    void idIsTheSameWhateverTheOrderOfTheTags() {
        String id1 = Battle.buildId(TIME, "#P0R9Y9J09", "#2VU8PGCC", "#9J8PRJRQL");
        String id2 = Battle.buildId(TIME, "#9J8PRJRQL", "#P0R9Y9J09", "#2VU8PGCC");

        assertThat(id1).isEqualTo(id2);
    }

    @Test
    void idIgnoresMissingPlayers() {
        String id = Battle.buildId(TIME, "#P0R9Y9J09", null, "#9J8PRJRQL", null);

        assertThat(id).isEqualTo("20261007T070930.000Z_#9J8PRJRQL");
    }
}