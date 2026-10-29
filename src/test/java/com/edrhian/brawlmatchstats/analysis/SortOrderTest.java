package com.edrhian.brawlmatchstats.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortOrderTest {

    @Test
    void readsFieldAndDirection() {
        SortOrder order = SortOrder.parse("winrate:asc");

        assertThat(order.field()).isEqualTo(BrawlerSort.WINRATE);
        assertThat(order.direction()).isEqualTo(SortDirection.ASC);
        assertThat(order.asText()).isEqualTo("winrate:asc");
    }

    @Test
    void directionIsOptionalAndUsesTheDefaultOfTheField() {
        assertThat(SortOrder.parse("winrate").direction()).isEqualTo(SortDirection.DESC);
        assertThat(SortOrder.parse("games").direction()).isEqualTo(SortDirection.DESC);
        assertThat(SortOrder.parse("confidence").direction()).isEqualTo(SortDirection.DESC);
        assertThat(SortOrder.parse("brawler").direction()).isEqualTo(SortDirection.ASC);
    }

    @Test
    void ignoresUpperAndLowerCaseAndSpaces() {
        SortOrder order = SortOrder.parse(" WilsonLower : DESC ");

        assertThat(order.field()).isEqualTo(BrawlerSort.WILSON_LOWER);
        assertThat(order.direction()).isEqualTo(SortDirection.DESC);
        assertThat(order.asText()).isEqualTo("wilsonLower:desc");
    }

    @Test
    void rejectsAnUnknownFieldAndListsTheAllowedOnes() {
        assertThatThrownBy(() -> SortOrder.parse("kills"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kills")
                .hasMessageContaining("winrate")
                .hasMessageContaining("confidence");
    }

    @Test
    void rejectsAnInvalidDirection() {
        assertThatThrownBy(() -> SortOrder.parse("winrate:up"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asc");
        assertThatThrownBy(() -> SortOrder.parse("winrate:"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsABadlyWrittenSort() {
        assertThatThrownBy(() -> SortOrder.parse("winrate:asc:games"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortOrder.parse(":asc"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}