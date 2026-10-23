package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Counts wins, losses and draws of each Brawler from a list of already filtered battles.
 *
 * Every Brawler gets the result of its own team. The battle result is stored from the point
 * of view of the ally team, so for the enemy team it is the opposite: if the ally team loses,
 * the 3 enemy Brawlers get a win.
 *
 * - A Brawler counts once per team and battle, even if it is repeated in the same team.
 * - Battles without a known result (victory, defeat, draw) are ignored.
 * - Brawler names are compared ignoring upper and lower case.
 */
public final class BrawlerStatsCalculator {

    private BrawlerStatsCalculator() {
        // Only static methods, nobody needs to create this class
    }

    /**
     * @param input optional brawlers: "with" must be teammates and "against" must be in the other team.
     *              The brawlers of "with" are not listed (they would be their own partner).
     * @return most played first, alphabetical when there is a tie
     */
    public static List<BrawlerStatsDto> brawlers(List<Battle> battles, BrawlerInput input, int minGames) {
        Map<String, Tally> tallies = new HashMap<>();

        for (Battle battle : battles) {
            List<String> allyTeam = Arrays.asList(
                    battle.getAlly1Brawler(), battle.getAlly2Brawler(), battle.getAlly3Brawler());
            List<String> enemyTeam = Arrays.asList(
                    battle.getEnemy1Brawler(), battle.getEnemy2Brawler(), battle.getEnemy3Brawler());
            String allyResult = battle.getBattleResult();

            countTeam(tallies, allyTeam, enemyTeam, allyResult, input);
            countTeam(tallies, enemyTeam, allyTeam, opposite(allyResult), input);
        }

        return tallies.entrySet().stream()
                .map(entry -> BrawlerStatsDto.of(entry.getKey(),
                        entry.getValue().wins, entry.getValue().losses, entry.getValue().draws))
                .filter(stats -> stats.games() >= minGames)
                .sorted(Comparator.comparingInt(BrawlerStatsDto::games).reversed()
                        .thenComparing(BrawlerStatsDto::brawler))
                .toList();
    }

    // Counts the result of one team for each of its Brawlers
    private static void countTeam(Map<String, Tally> tallies, List<String> team, List<String> opponents,
                                  String teamResult, BrawlerInput input) {
        // "against": all these Brawlers must be in the other team
        if (!containsAll(opponents, input.against())) {
            return;
        }

        // The Set removes a Brawler that is repeated in the same team
        for (String brawler : new LinkedHashSet<>(team)) {
            if (brawler == null || containsIgnoreCase(input.with(), brawler)) {
                continue;
            }

            // "with": all these Brawlers must be teammates (the Brawler itself is not its own teammate)
            List<String> teammates = new ArrayList<>(team);
            teammates.remove(brawler);
            if (containsAll(teammates, input.with())) {
                tallies.computeIfAbsent(brawler, name -> new Tally()).add(teamResult);
            }
        }
    }

    // The result for the other team. Unknown results stay unknown (null) and are not counted
    private static String opposite(String result) {
        if (result == null) {
            return null;
        }
        return switch (result) {
            case "victory" -> "defeat";
            case "defeat" -> "victory";
            case "draw" -> "draw";
            default -> null;
        };
    }

    private static boolean containsAll(List<String> team, List<String> wanted) {
        return wanted.stream().allMatch(name -> containsIgnoreCase(team, name));
    }

    // The list can contain nulls when a slot is empty
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