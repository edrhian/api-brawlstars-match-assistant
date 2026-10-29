package com.edrhian.brawlmatchstats.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns the sort values of a request into the order of the results.
 *
 * - Without sort the order is: most played first (games:desc).
 * - The first sort has the priority, the next ones only break the ties.
 * - The brawler name is always the last criteria, so the same data always gives the same order.
 */
public final class BrawlerSorting {

    private BrawlerSorting() {
        // Only static methods, nobody needs to create this class
    }

    /**
     * Reads and checks the sort values, and adds the default and the final name criteria.
     *
     * @param rawSorts values like winrate:asc (they can be null or empty)
     * @return the sort criteria that are really used, never empty
     * @throws IllegalArgumentException for an unknown field or direction, or a repeated field
     */
    public static List<SortOrder> resolve(List<String> rawSorts) {
        List<SortOrder> orders = new ArrayList<>();

        if (rawSorts != null) {
            for (String raw : rawSorts) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                SortOrder order = SortOrder.parse(raw);
                if (containsField(orders, order.field())) {
                    throw new IllegalArgumentException("sort field '" + order.field().field() + "' is repeated");
                }
                orders.add(order);
            }
        }

        if (orders.isEmpty()) {
            orders.add(new SortOrder(BrawlerSort.GAMES, SortDirection.DESC));
        }
        if (!containsField(orders, BrawlerSort.BRAWLER)) {
            orders.add(new SortOrder(BrawlerSort.BRAWLER, SortDirection.ASC));
        }
        return List.copyOf(orders);
    }

    // One comparator that applies the criteria in order
    public static Comparator<BrawlerStatsDto> comparator(List<SortOrder> orders) {
        Comparator<BrawlerStatsDto> result = Comparator.comparing(BrawlerStatsDto::brawler);
        boolean first = true;
        for (SortOrder order : orders) {
            Comparator<BrawlerStatsDto> next = order.field().comparator(order.direction());
            result = first ? next : result.thenComparing(next);
            first = false;
        }
        return result;
    }

    private static boolean containsField(List<SortOrder> orders, BrawlerSort field) {
        return orders.stream().anyMatch(order -> order.field() == field);
    }
}