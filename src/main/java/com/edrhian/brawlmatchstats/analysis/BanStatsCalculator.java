package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Counts the bans of a list of already filtered battles.
 *
 * - Only battles with at least one ban count for the banrate (battles without draft have none).
 * - A brawler is banned once per battle, even if several players banned it.
 *   The individual bans are counted separately in "bans".
 * - The most banned brawler goes first. Ties: more individual bans, then alphabetical.
 */
public final class BanStatsCalculator {

    private BanStatsCalculator() {
        // Only static methods, nobody needs to create this class
    }

    public static BanAnalysisResponse bans(List<Battle> battles) {
        Map<String, Counter> counters = new HashMap<>();
        int battlesWithBans = 0;

        for (Battle battle : battles) {
            List<String> bans = bansOf(battle);
            if (bans.isEmpty()) {
                continue;
            }
            battlesWithBans++;

            // Every individual ban
            for (String brawler : bans) {
                counters.computeIfAbsent(brawler, name -> new Counter()).bans++;
            }
            // Once per battle: the Set removes the brawlers banned by more than one player
            for (String brawler : new LinkedHashSet<>(bans)) {
                counters.get(brawler).bannedBattles++;
            }
        }

        final int total = battlesWithBans;
        List<BanStatsDto> rows = counters.entrySet().stream()
                .map(entry -> BanStatsDto.of(entry.getKey(),
                        entry.getValue().bans, entry.getValue().bannedBattles, total))
                .sorted(Comparator.comparingInt(BanStatsDto::bannedBattles).reversed()
                        .thenComparing(Comparator.comparingInt(BanStatsDto::bans).reversed())
                        .thenComparing(BanStatsDto::brawler))
                .toList();

        return new BanAnalysisResponse(battles.size(), total, Confidence.of(total), rows);
    }

    // The bans of the 6 players that exist (empty slots and blank names are ignored)
    private static List<String> bansOf(Battle battle) {
        return Arrays.asList(
                        battle.getAlly1Ban(), battle.getAlly2Ban(), battle.getAlly3Ban(),
                        battle.getEnemy1Ban(), battle.getEnemy2Ban(), battle.getEnemy3Ban())
                .stream()
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .toList();
    }

    // Counter of bans
    private static final class Counter {
        private int bans;
        private int bannedBattles;
    }
}