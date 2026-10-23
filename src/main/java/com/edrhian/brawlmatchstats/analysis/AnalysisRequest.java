package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;
import com.edrhian.brawlmatchstats.shared.TagUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The query parameters of the analysis endpoints, exactly as they arrive.
 * Spring fills it from the URL: ?players=2VU8PGCC&players=P0PULRC&battleTypes=soloRanked&limit=100
 *
 * Players are required: either players=... or allPlayers=true. Everything else is optional.
 * toFilter() and toInput() check the values and convert them.
 */
public record AnalysisRequest(
        List<String> players,
        Boolean allPlayers,
        List<BattleType> battleTypes,
        List<String> ranks,
        Integer trophiesMin,
        Integer trophiesMax,
        List<String> gameModes,
        List<String> maps,
        String from,
        String to,
        Integer limit,
        Integer minGames,
        List<String> with,
        List<String> against
) {

    private static final int MAX_WITH = 2;     // a team has 3 brawlers and one of them is the one we count
    private static final int MAX_AGAINST = 3;

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

        boolean everyone = Boolean.TRUE.equals(allPlayers);
        return new BattleFilter(
                resolvePlayers(players, everyone),
                everyone,
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

    // The brawlers that narrow down the analysis (both are optional)
    public BrawlerInput toInput() {
        return new BrawlerInput(
                cleanBrawlers(with, MAX_WITH, "with"),
                cleanBrawlers(against, MAX_AGAINST, "against"));
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

    /**
     * Players are required: tags (stored like #2VU8PGCC) or allPlayers=true, but not both.
     * With allPlayers the list is empty.
     */
    static List<String> resolvePlayers(List<String> rawTags, boolean allPlayers) {
        List<String> tags = cleanText(rawTags).stream().map(TagUtils::normalize).distinct().toList();
        if (allPlayers && !tags.isEmpty()) {
            throw new IllegalArgumentException("Use either players or allPlayers=true, not both");
        }
        if (!allPlayers && tags.isEmpty()) {
            throw new IllegalArgumentException(
                    "Choose players (for example players=2VU8PGCC) or use allPlayers=true");
        }
        return tags;
    }

    // Brawler names as the API writes them (uppercase), without repeated ones
    private static List<String> cleanBrawlers(List<String> rawBrawlers, int max, String parameter) {
        List<String> brawlers = cleanText(rawBrawlers).stream()
                .map(name -> name.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        if (brawlers.size() > max) {
            throw new IllegalArgumentException(parameter + " accepts up to " + max + " different brawlers");
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