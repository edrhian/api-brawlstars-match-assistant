package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Counts wins, losses and draws from a list of already filtered battles.
 *
 * The result of a battle is always from the point of view of the ally team, so:
 * - for ally Brawlers it is how well we do when we pick them
 * - for enemy Brawlers it is how well we do when we play against them
 *
 * A Brawler counts once per battle, even if it appears twice in the same team.
 * Battles without a known result (victory, defeat, draw) are ignored.
 * Brawler names are compared ignoring upper and lower case.
 */
public final class BrawlerStatsCalculator {

    private BrawlerStatsCalculator() {
        // Only static methods, nobody needs to create this class
    }

    public static StatsDto overall(List<Battle> battles) {
        Tally tally = new Tally();
        for (Battle battle : battles) {
            tally.add(battle.getBattleResult());
        }
        return StatsDto.of(tally.wins, tally.losses, tally.draws);
    }

    // ---------- Part 1: results of each Brawler ----------

    public static List<BrawlerStatsDto> allyBrawlers(List<Battle> battles, int minGames) {
        return byBrawler(battles, minGames, BrawlerStatsCalculator::allyBrawlersOf);
    }

    public static List<BrawlerStatsDto> enemyBrawlers(List<Battle> battles, int minGames) {
        return byBrawler(battles, minGames, BrawlerStatsCalculator::enemyBrawlersOf);
    }

    // ---------- Part 3: results depending on an input of Brawlers ----------

    // The battles where ALL these Brawlers were in the ally team
    public static List<Battle> withAllyBrawlers(List<Battle> battles, List<String> brawlers) {
        return battles.stream().filter(battle -> containsAll(allyBrawlersOf(battle), brawlers)).toList();
    }

    // The battles where ALL these Brawlers were in the enemy team
    public static List<Battle> withEnemyBrawlers(List<Battle> battles, List<String> brawlers) {
        return battles.stream().filter(battle -> containsAll(enemyBrawlersOf(battle), brawlers)).toList();
    }

    /**
     * Ally synergy. Takes the battles that have all the input Brawlers in the ally team and
     * counts each other ally Brawler separately: with input SHELLY, a battle of SHELLY + COLT + SPIKE
     * counts for [SHELLY + COLT] and also for [SHELLY + SPIKE], and COLT + SPIKE is not a fixed pair.
     */
    public static List<BrawlerStatsDto> allyPartners(List<Battle> battles, List<String> inputBrawlers, int minGames) {
        return byBrawler(withAllyBrawlers(battles, inputBrawlers), minGames,
                battle -> allyBrawlersOf(battle).stream()
                        .filter(brawler -> brawler != null && !containsIgnoreCase(inputBrawlers, brawler))
                        .toList());
    }

    /**
     * Counter analysis. Takes the battles that have all the input Brawlers in the enemy team and
     * counts each ally Brawler separately: how well each pick does against them.
     */
    public static List<BrawlerStatsDto> countersAgainst(List<Battle> battles, List<String> inputEnemies, int minGames) {
        return byBrawler(withEnemyBrawlers(battles, inputEnemies), minGames,
                BrawlerStatsCalculator::allyBrawlersOf);
    }

    // ---------- Helpers ----------

    // Most played first, and alphabetical when there is a tie
    private static List<BrawlerStatsDto> byBrawler(List<Battle> battles, int minGames,
                                                   Function<Battle, List<String>> brawlersOf) {
        Map<String, Tally> tallies = new HashMap<>();

        for (Battle battle : battles) {
            // The Set removes a Brawler that is repeated in the same team
            for (String brawler : new LinkedHashSet<>(brawlersOf.apply(battle))) {
                if (brawler != null) {
                    tallies.computeIfAbsent(brawler, name -> new Tally()).add(battle.getBattleResult());
                }
            }
        }

        return tallies.entrySet().stream()
                .map(entry -> BrawlerStatsDto.of(entry.getKey(),
                        entry.getValue().wins, entry.getValue().losses, entry.getValue().draws))
                .filter(stats -> stats.games() >= minGames)
                .sorted(Comparator.comparingInt(BrawlerStatsDto::games).reversed()
                        .thenComparing(BrawlerStatsDto::brawler))
                .toList();
    }

    // The list can contain nulls when a slot is empty
    private static List<String> allyBrawlersOf(Battle battle) {
        return Arrays.asList(battle.getAlly1Brawler(), battle.getAlly2Brawler(), battle.getAlly3Brawler());
    }

    private static List<String> enemyBrawlersOf(Battle battle) {
        return Arrays.asList(battle.getEnemy1Brawler(), battle.getEnemy2Brawler(), battle.getEnemy3Brawler());
    }

    private static boolean containsAll(List<String> team, List<String> wanted) {
        return wanted.stream().allMatch(name -> containsIgnoreCase(team, name));
    }

    private static boolean containsIgnoreCase(List<String> names, String name) {
        return names.stream().anyMatch(other -> other != null && other.equalsIgnoreCase(name));
    }

    // Counter of results
    private static final class Tally {
        private int wins;
        private int losses;
        private int draws;

        void add(String result) {
            if (result == null) {
                return;
            }
            switch (result) {
                case "victory" -> wins++;
                case "defeat" -> losses++;
                case "draw" -> draws++;
                default -> {
                    // Unknown result: not counted
                }
            }
        }
    }
}