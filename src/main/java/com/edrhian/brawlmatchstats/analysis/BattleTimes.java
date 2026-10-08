package com.edrhian.brawlmatchstats.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converts dates between what the API user writes (2026-10-01) and what the database stores
 * (20261001T000000.000Z). The stored format has a fixed width, so comparing it as text is the
 * same as comparing the dates. All times are UTC.
 */
public final class BattleTimes {

    private static final DateTimeFormatter STORED = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private BattleTimes() {
        // Only static methods, nobody needs to create this class
    }

    /**
     * Accepts 2026-10-01 or 2026-10-01T10:30:00. A date without time means the start of that day,
     * or the end of that day when it is the end of a range.
     * Returns null when the value is empty.
     */
    public static String toStored(String value, boolean endOfRange) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String text = value.trim();
        try {
            LocalDateTime dateTime;
            if (text.length() == 10) {
                LocalDate date = LocalDate.parse(text);
                dateTime = endOfRange ? date.atTime(23, 59, 59, 999_000_000) : date.atStartOfDay();
            } else {
                dateTime = LocalDateTime.parse(text);
            }
            return dateTime.format(STORED);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date '" + value + "'. Use 2026-10-01 or 2026-10-01T10:30:00 (UTC)");
        }
    }

    // 20261001T100000.000Z -> 2026-10-01T10:00:00
    public static String toIso(String stored) {
        if (stored == null) {
            return null;
        }
        return LocalDateTime.parse(stored, STORED).format(ISO);
    }
}