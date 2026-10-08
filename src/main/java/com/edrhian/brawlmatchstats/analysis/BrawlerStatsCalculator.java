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

    public static List<BrawlerStatsDto> allyBrawlers(List<Battle> battles, int minGames) {
        return byBrawler(battles, minGames,
                battle -> Arrays.asList(battle.getAlly1Brawler(), battle.getAlly2Brawler(), battle.getAlly3Brawler()));
    }

    public static List<BrawlerStatsDto> enemyBrawlers(List<Battle> battles, int minGames) {
        return byBrawler(battles, minGames,
                battle -> Arrays.asList(battle.getEnemy1Brawler(), battle.getEnemy2Brawler(), battle.getEnemy3Brawler()));
    }

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