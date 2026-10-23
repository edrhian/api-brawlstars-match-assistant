package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrawlerStatsCalculatorTest {

    // The same two battles for most tests:
    // 1. SHELLY COLT SPIKE beat BROCK CARL BYRON
    // 2. COLT AMBER SPIKE lose against CARL DARRYL BYRON
    private final List<Battle> battles = List.of(
            battle("victory", "SHELLY", "COLT", "SPIKE", "BROCK", "CARL", "BYRON"),
            battle("defeat", "COLT", "AMBER", "SPIKE", "CARL", "DARRYL", "BYRON"));

    // ---------- Winrate of every brawler ----------

    @Test
    void everyBrawlerGetsTheResultOfItsOwnTeam() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(battles, BrawlerInput.NONE, 1);

        // The ally team lost battle 2, so DARRYL (enemy) won it
        assertThat(find(stats, "DARRYL").wins()).isEqualTo(1);
        assertThat(find(stats, "DARRYL").winrate()).isEqualTo(100.0);
        // The ally team won battle 1, so BROCK (enemy) lost it
        assertThat(find(stats, "BROCK").losses()).isEqualTo(1);
        assertThat(find(stats, "BROCK").winrate()).isZero();
        // BYRON was an enemy in both: lost battle 1 and won battle 2
        assertThat(find(stats, "BYRON").games()).isEqualTo(2);
        assertThat(find(stats, "BYRON").winrate()).isEqualTo(50.0);
    }

    @Test
    void sortsByGamesAndThenByName() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(battles, BrawlerInput.NONE, 1);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly(
                "BYRON", "CARL", "COLT", "SPIKE", "AMBER", "BROCK", "DARRYL", "SHELLY");
    }

    @Test
    void minGamesHidesBrawlersWithFewerGames() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(battles, BrawlerInput.NONE, 2);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("BYRON", "CARL", "COLT", "SPIKE");
    }

    @Test
    void sameBrawlerOnBothSidesIsOneRow() {
        List<Battle> list = List.of(
                // COLT is an ally and wins
                battle("victory", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"),
                // COLT is an enemy and the ally team loses, so COLT wins again
                battle("defeat", "AMBER", "DARRYL", "PIPER", "COLT", "MORTIS", "POCO"));

        BrawlerStatsDto colt = find(BrawlerStatsCalculator.brawlers(list, BrawlerInput.NONE, 1), "COLT");

        assertThat(colt.games()).isEqualTo(2);
        assertThat(colt.wins()).isEqualTo(2);
        assertThat(colt.winrate()).isEqualTo(100.0);
    }

    @Test
    void drawsAreGamesButNotWinsForBothTeams() {
        List<Battle> list = List.of(battle("draw", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"));

        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(list, BrawlerInput.NONE, 1);

        assertThat(stats).hasSize(6);
        assertThat(stats).allSatisfy(stat -> {
            assertThat(stat.games()).isEqualTo(1);
            assertThat(stat.draws()).isEqualTo(1);
            assertThat(stat.wins()).isZero();
            assertThat(stat.winrate()).isZero();
        });
    }

    @Test
    void ignoresBattlesWithoutKnownResult() {
        List<Battle> list = List.of(
                battle(null, "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"),
                battle("weird", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"));

        assertThat(BrawlerStatsCalculator.brawlers(list, BrawlerInput.NONE, 1)).isEmpty();
    }

    @Test
    void countsABrawlerOncePerTeamAndBattle() {
        List<Battle> list = List.of(battle("victory", "COLT", "COLT", "AMBER", "BROCK", "CARL", "BYRON"));

        BrawlerStatsDto colt = find(BrawlerStatsCalculator.brawlers(list, BrawlerInput.NONE, 1), "COLT");

        assertThat(colt.games()).isEqualTo(1);
    }

    @Test
    void emptySlotsAreIgnored() {
        List<Battle> list = List.of(battle("victory", "COLT", null, null, "BROCK", null, null));

        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(list, BrawlerInput.NONE, 1);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("BROCK", "COLT");
    }

    // ---------- with: brawlers that must be teammates ----------

    @Test
    void withListsTheTeammatesOfTheInputBrawler() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(
                battles, new BrawlerInput(List.of("SHELLY"), List.of()), 1);

        // Only battle 1 has SHELLY, and SHELLY itself is not listed
        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
        assertThat(stats).allSatisfy(stat -> assertThat(stat.winrate()).isEqualTo(100.0));
    }

    @Test
    void withTwoBrawlersNeedsBothAsTeammates() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(
                battles, new BrawlerInput(List.of("SHELLY", "COLT"), List.of()), 1);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("SPIKE");
    }

    @Test
    void inputNamesAreComparedIgnoringCase() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(
                battles, new BrawlerInput(List.of("shelly"), List.of()), 1);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
    }

    // ---------- against: brawlers that must be in the other team ----------

    @Test
    void againstListsTheBrawlersThatPlayedAgainstTheInputBrawler() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(
                battles, new BrawlerInput(List.of(), List.of("BROCK")), 1);

        // BROCK was only in battle 1, in the enemy team. The ally team won
        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SHELLY", "SPIKE");
        assertThat(stats).allSatisfy(stat -> assertThat(stat.wins()).isEqualTo(1));
    }

    @Test
    void againstWorksWhenTheInputBrawlerIsInTheAllyTeam() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(
                battles, new BrawlerInput(List.of(), List.of("SHELLY")), 1);

        // SHELLY was an ally in battle 1: the enemies BROCK, CARL and BYRON lost against it
        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("BROCK", "BYRON", "CARL");
        assertThat(stats).allSatisfy(stat -> assertThat(stat.winrate()).isZero());
    }

    @Test
    void withAndAgainstCanBeCombined() {
        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.brawlers(
                battles, new BrawlerInput(List.of("SHELLY"), List.of("BROCK")), 1);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
    }

    // ---------- Helpers ----------

    private static BrawlerStatsDto find(List<BrawlerStatsDto> stats, String brawler) {
        return stats.stream().filter(stat -> stat.brawler().equals(brawler)).findFirst().orElseThrow();
    }

    private static Battle battle(String result, String a1, String a2, String a3, String e1, String e2, String e3) {
        Battle battle = new Battle();
        battle.setBattleResult(result);
        battle.setAlly1Brawler(a1);
        battle.setAlly2Brawler(a2);
        battle.setAlly3Brawler(a3);
        battle.setEnemy1Brawler(e1);
        battle.setEnemy2Brawler(e2);
        battle.setEnemy3Brawler(e3);
        return battle;
    }
}