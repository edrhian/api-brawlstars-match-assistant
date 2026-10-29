package com.edrhian.brawlmatchstats.analysis;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrawlerSortingTest {

    // AMBER:   10 games, 80.0%, LOW
    // BROCK:    3 games, 100.0%, INSUFFICIENT
    // COLT:   100 games, 60.0%, HIGH
    // DARRYL:  10 games, 50.0%, LOW
    private final List<BrawlerStatsDto> stats = List.of(
            BrawlerStatsDto.of("AMBER", 8, 2, 0),
            BrawlerStatsDto.of("BROCK", 3, 0, 0),
            BrawlerStatsDto.of("COLT", 60, 40, 0),
            BrawlerStatsDto.of("DARRYL", 5, 5, 0));

    // ---------- resolve ----------

    @Test
    void withoutSortTheDefaultIsMostPlayedAndThenName() {
        assertThat(texts(BrawlerSorting.resolve(null))).containsExactly("games:desc", "brawler:asc");
        assertThat(texts(BrawlerSorting.resolve(List.of()))).containsExactly("games:desc", "brawler:asc");
        assertThat(texts(BrawlerSorting.resolve(Arrays.asList(" ", null)))).containsExactly("games:desc", "brawler:asc");
    }

    @Test
    void theNameIsAlwaysAddedAtTheEnd() {
        assertThat(texts(BrawlerSorting.resolve(List.of("winrate", "games:asc"))))
                .containsExactly("winrate:desc", "games:asc", "brawler:asc");
    }

    @Test
    void theNameIsNotAddedWhenItIsAlreadyThere() {
        assertThat(texts(BrawlerSorting.resolve(List.of("brawler:desc")))).containsExactly("brawler:desc");
    }

    @Test
    void rejectsARepeatedField() {
        assertThatThrownBy(() -> BrawlerSorting.resolve(List.of("winrate", "winrate:asc")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("winrate");
    }

    @Test
    void rejectsAnInvalidSort() {
        assertThatThrownBy(() -> BrawlerSorting.resolve(List.of("kills")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void everyFieldCanBeUsed() {
        for (BrawlerSort sort : BrawlerSort.values()) {
            assertThat(BrawlerSorting.resolve(List.of(sort.field())).get(0).field()).isEqualTo(sort);
        }
    }

    // ---------- comparator ----------

    @Test
    void defaultOrderIsMostPlayedAndThenName() {
        // AMBER and DARRYL have the same games, so the name breaks the tie
        assertThat(sorted()).containsExactly("COLT", "AMBER", "DARRYL", "BROCK");
    }

    @Test
    void sortsByWinrateWithTheBestFirst() {
        assertThat(sorted("winrate")).containsExactly("BROCK", "AMBER", "COLT", "DARRYL");
    }

    @Test
    void directionCanBeChanged() {
        assertThat(sorted("winrate:asc")).containsExactly("DARRYL", "COLT", "AMBER", "BROCK");
        assertThat(sorted("brawler:desc")).containsExactly("DARRYL", "COLT", "BROCK", "AMBER");
    }

    @Test
    void confidenceIsSortedByLevelNotByName() {
        // Alphabetically HIGH would be first and INSUFFICIENT second, but the level is what counts
        assertThat(sorted("confidence")).containsExactly("COLT", "AMBER", "DARRYL", "BROCK");
        assertThat(sorted("confidence:asc")).containsExactly("BROCK", "AMBER", "DARRYL", "COLT");
    }

    @Test
    void combinesSortsInOrderOfPriority() {
        // Confidence first, then winrate (AMBER 80% before DARRYL 50%), then games
        assertThat(sorted("confidence", "winrate", "games")).containsExactly("COLT", "AMBER", "DARRYL", "BROCK");
        // Games first (ascending), the ones with 10 games are ordered by winrate
        assertThat(sorted("games:asc", "winrate")).containsExactly("BROCK", "AMBER", "DARRYL", "COLT");
    }

    // ---------- Helpers ----------

    private List<String> sorted(String... sorts) {
        return stats.stream()
                .sorted(BrawlerSorting.comparator(BrawlerSorting.resolve(List.of(sorts))))
                .map(BrawlerStatsDto::brawler)
                .toList();
    }

    private static List<String> texts(List<SortOrder> orders) {
        return orders.stream().map(SortOrder::asText).toList();
    }
}