package com.edrhian.brawlmatchstats.analysis;

import java.util.Locale;

/**
 * Sort direction of one sort field.
 */
public enum SortDirection {
    ASC,
    DESC;

    // asc or desc, ignoring upper and lower case
    public static SortDirection parse(String text) {
        for (SortDirection direction : values()) {
            if (direction.name().equalsIgnoreCase(text)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid sort direction '" + text + "'. Use asc or desc");
    }

    // asc or desc, as it is written in the API
    public String lower() {
        return name().toLowerCase(Locale.ROOT);
    }
}