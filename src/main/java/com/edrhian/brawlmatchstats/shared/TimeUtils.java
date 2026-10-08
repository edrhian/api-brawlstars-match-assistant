package com.edrhian.brawlmatchstats.shared;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Helper for the time Strings we store, like 2026-10-07T11:00:39 (always in UTC).
 */
public final class TimeUtils {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private TimeUtils() {
        // Only static methods, nobody needs to create this class
    }

    public static String nowAsString() {
        return LocalDateTime.now(ZoneOffset.UTC).format(FORMAT);
    }
}