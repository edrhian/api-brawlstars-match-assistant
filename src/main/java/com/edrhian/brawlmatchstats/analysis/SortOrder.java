package com.edrhian.brawlmatchstats.analysis;

/**
 * One sort criteria: a field and a direction, like winrate:asc.
 */
public record SortOrder(BrawlerSort field, SortDirection direction) {

    /**
     * Reads field or field:direction (winrate, winrate:asc, games:desc).
     * Without a direction, the field uses its default one.
     *
     * @throws IllegalArgumentException if the text is not valid (it becomes a 400 error)
     */
    public static SortOrder parse(String text) {
        String[] parts = text.trim().split(":", -1);
        if (parts.length > 2 || parts[0].isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid sort '" + text + "'. Use field or field:direction, for example winrate:asc");
        }

        BrawlerSort field = BrawlerSort.fromField(parts[0].trim());
        SortDirection direction = parts.length == 2
                ? SortDirection.parse(parts[1].trim())
                : field.defaultDirection();
        return new SortOrder(field, direction);
    }

    // The same text the API accepts, example: winrate:asc
    public String asText() {
        return field.field() + ":" + direction.lower();
    }
}