package com.edrhian.brawlmatchstats.analysis;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * The fields that can be used to sort the brawlers of the analysis.
 *
 * - field: the name used in the API (sort=winrate)
 * - defaultDirection: the direction when the request does not write one (sort=winrate)
 * - natural: how to compare two brawlers from lowest to highest
 */
public enum BrawlerSort {

    BRAWLER("brawler", SortDirection.ASC, Comparator.comparing(BrawlerStatsDto::brawler)),
    GAMES("games", SortDirection.DESC, Comparator.comparingInt(BrawlerStatsDto::games)),
    WINS("wins", SortDirection.DESC, Comparator.comparingInt(BrawlerStatsDto::wins)),
    LOSSES("losses", SortDirection.DESC, Comparator.comparingInt(BrawlerStatsDto::losses)),
    DRAWS("draws", SortDirection.DESC, Comparator.comparingInt(BrawlerStatsDto::draws)),
    WINRATE("winrate", SortDirection.DESC, Comparator.comparingDouble(BrawlerStatsDto::winrate)),
    WILSON_LOWER("wilsonLower", SortDirection.DESC, Comparator.comparingDouble(BrawlerStatsDto::wilsonLower)),
    WILSON_UPPER("wilsonUpper", SortDirection.DESC, Comparator.comparingDouble(BrawlerStatsDto::wilsonUpper)),
    // Compared by level (INSUFFICIENT < LOW < MEDIUM < HIGH), not alphabetically.
    // That is the order in which Confidence is declared, so do not reorder that enum.
    CONFIDENCE("confidence", SortDirection.DESC, Comparator.comparing(BrawlerStatsDto::confidence));

    private final String field;
    private final SortDirection defaultDirection;
    private final Comparator<BrawlerStatsDto> natural;

    BrawlerSort(String field, SortDirection defaultDirection, Comparator<BrawlerStatsDto> natural) {
        this.field = field;
        this.defaultDirection = defaultDirection;
        this.natural = natural;
    }

    public String field() {
        return field;
    }

    public SortDirection defaultDirection() {
        return defaultDirection;
    }

    public Comparator<BrawlerStatsDto> comparator(SortDirection direction) {
        return direction == SortDirection.ASC ? natural : natural.reversed();
    }

    // The name written in the API, ignoring upper and lower case (winrate, wilsonLower...)
    public static BrawlerSort fromField(String text) {
        for (BrawlerSort sort : values()) {
            if (sort.field.equalsIgnoreCase(text)) {
                return sort;
            }
        }
        String allowed = Arrays.stream(values()).map(BrawlerSort::field).collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Cannot sort by '" + text + "'. Allowed fields: " + allowed);
    }
}