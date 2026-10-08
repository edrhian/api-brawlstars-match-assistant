package com.edrhian.brawlmatchstats.shared;

/**
 * Helper to write player tags always in the same way: uppercase and starting with "#".
 */
public final class TagUtils {

    private TagUtils() {
        // Only static methods, nobody needs to create this class
    }

    public static String normalize(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("The player tag cannot be empty");
        }
        String clean = tag.trim().toUpperCase();
        return clean.startsWith("#") ? clean : "#" + clean;
    }
}