package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;
import com.edrhian.brawlmatchstats.shared.TagUtils;

import java.util.List;
import java.util.Objects;
import java.util.Locale;

/**
 * The query parameters of the analysis endpoints, exactly as they arrive.
 * Spring fills it from the URL: ?players=2VU8PGCC&players=P0PULRC&battleTypes=soloRanked&limit=100
 *
 * Everything is optional except players. toFilter() checks the values and converts them.
 */
public record AnalysisRequest(
        List<String> players,
        List<BattleType> battleTypes,
        List<String> ranks,
        Integer trophiesMin,
        Integer trophiesMax,
        List<String> gameModes,
        List<String> maps,
        String from,
        String to,
        Integer limit,
        Integer minGames
) {

    /**
     * @throws IllegalArgumentException if something is wrong (it becomes a 400 error)
     */
    public BattleFilter toFilter() {
        if (limit != null && limit < 1) {
            throw new IllegalArgumentException("limit must be 1 or more");
        }
        if (trophiesMin != null && trophiesMax != null && trophiesMin > trophiesMax) {
            throw new IllegalArgumentException("trophiesMin cannot be greater than trophiesMax");
        }

        String fromTime = BattleTimes.toStored(from, false);
        String toTime = BattleTimes.toStored(to, true);
        if (fromTime != null && toTime != null && fromTime.compareTo(toTime) > 0) {
            throw new IllegalArgumentException("from cannot be after to");
        }

        return new BattleFilter(
                normalizeTags(players),
                cleanTypes(battleTypes),
                cleanText(ranks),
                trophiesMin,
                trophiesMax,
                cleanText(gameModes),
                cleanText(maps),
                fromTime,
                toTime,
                limit);
    }

    // Minimum number of games a Brawler needs to appear in the results (default 1)
    public int minGamesOrDefault() {
        if (minGames == null) {
            return 1;
        }
        if (minGames < 1) {
            throw new IllegalArgumentException("minGames must be 1 or more");
        }
        return minGames;
    }

    // Tags in the same way we store them (#2VU8PGCC), without repeated or empty ones
    static List<String> normalizeTags(List<String> rawTags) {
        List<String> tags = cleanText(rawTags).stream().map(TagUtils::normalize).distinct().toList();
        if (tags.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required, for example players=2VU8PGCC");
        }
        return tags;
    }

    // Brawler names as the API writes them (uppercase), 1 to 3 different ones
    static List<String> cleanBrawlers(List<String> rawBrawlers) {
        List<String> brawlers = cleanText(rawBrawlers).stream()
                .map(name -> name.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        if (brawlers.isEmpty() || brawlers.size() > 3) {
            throw new IllegalArgumentException(
                    "Choose between 1 and 3 different brawlers, for example brawlers=SHELLY");
        }
        return brawlers;
    }

    private static List<String> cleanText(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }

    private static List<BattleType> cleanTypes(List<BattleType> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
    }
}