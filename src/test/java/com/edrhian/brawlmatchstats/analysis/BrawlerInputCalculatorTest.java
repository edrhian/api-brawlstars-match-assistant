package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrawlerInputCalculatorTest {

    private static final String[] ENEMIES = team("BROCK", "CARL", "BYRON");

    // ---------- Ally synergy ----------

    @Test
    void partnersAreCountedSeparately() {
        List<Battle> battles = synergyBattles();

        List<BrawlerStatsDto> partners = BrawlerStatsCalculator.allyPartners(battles, List.of("SHELLY"), 1);

        // SHELLY + COLT + SPIKE counts for [SHELLY + COLT] and also for [SHELLY + SPIKE]
        // The input Brawler is never a partner of itself, and the battle without SHELLY is ignored
        assertThat(partners).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE", "AMBER");
        assertThat(partners.get(0).games()).isEqualTo(2);
        assertThat(partners.get(0).wins()).isEqualTo(1);
        assertThat(partners.get(0).winrate()).isEqualTo(50.0);
        assertThat(partners.get(1).games()).isEqualTo(2);
        assertThat(partners.get(1).winrate()).isEqualTo(100.0);
        assertThat(partners.get(2).losses()).isEqualTo(1);
    }

    @Test
    void twoInputBrawlersNeedToBeInTheSameTeam() {
        List<BrawlerStatsDto> partners = BrawlerStatsCalculator.allyPartners(
                synergyBattles(), List.of("SHELLY", "COLT"), 1);

        assertThat(partners).extracting(BrawlerStatsDto::brawler).containsExactly("AMBER", "SPIKE");
    }

    @Test
    void inputBrawlersAreComparedIgnoringCase() {
        List<BrawlerStatsDto> partners = BrawlerStatsCalculator.allyPartners(
                synergyBattles(), List.of("shelly"), 1);

        assertThat(partners).hasSize(3);
    }

    @Test
    void baselineIsTheBattlesThatContainAllTheInputBrawlers() {
        List<Battle> battles = synergyBattles();

        assertThat(BrawlerStatsCalculator.withAllyBrawlers(battles, List.of("SHELLY"))).hasSize(3);
        assertThat(BrawlerStatsCalculator.withAllyBrawlers(battles, List.of("SHELLY", "COLT"))).hasSize(2);
        assertThat(BrawlerStatsCalculator.withAllyBrawlers(battles, List.of("SHELLY", "COLT", "AMBER"))).hasSize(1);
    }

    @Test
    void partnersBelowMinGamesAreHidden() {
        List<BrawlerStatsDto> partners = BrawlerStatsCalculator.allyPartners(
                synergyBattles(), List.of("SHELLY"), 2);

        assertThat(partners).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
    }

    // ---------- Enemy counters ----------

    @Test
    void countersAreTheAllyPicksAgainstTheInputEnemy() {
        List<BrawlerStatsDto> counters = BrawlerStatsCalculator.countersAgainst(
                counterBattles(), List.of("BROCK"), 1);

        // The third battle has no BROCK in the enemy team, so it is ignored
        assertThat(counters).extracting(BrawlerStatsDto::brawler)
                .containsExactly("COLT", "SPIKE", "AMBER", "SHELLY");
        assertThat(counters.get(0).games()).isEqualTo(2);
        assertThat(counters.get(0).winrate()).isEqualTo(50.0);
        assertThat(counters.get(3).winrate()).isEqualTo(100.0);
    }

    @Test
    void counterInputNeedsAllTheEnemiesInTheSameTeam() {
        List<BrawlerStatsDto> counters = BrawlerStatsCalculator.countersAgainst(
                counterBattles(), List.of("BROCK", "CARL"), 1);

        assertThat(counters).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SHELLY", "SPIKE");
        assertThat(BrawlerStatsCalculator.withEnemyBrawlers(counterBattles(), List.of("BROCK", "CARL"))).hasSize(1);
    }

    @Test
    void allyPickEqualToTheEnemyInputStillCounts() {
        List<Battle> battles = List.of(
                battle("victory", team("BROCK", "COLT", "SPIKE"), team("BROCK", "CARL", "BYRON")));

        List<BrawlerStatsDto> counters = BrawlerStatsCalculator.countersAgainst(battles, List.of("BROCK"), 1);

        assertThat(counters).extracting(BrawlerStatsDto::brawler).contains("BROCK");
    }

    // ---------- Helpers ----------

    private static List<Battle> synergyBattles() {
        return List.of(
                battle("victory", team("SHELLY", "COLT", "SPIKE"), ENEMIES),
                battle("defeat", team("SHELLY", "COLT", "AMBER"), ENEMIES),
                battle("victory", team("COLT", "SPIKE", "AMBER"), ENEMIES),   // no SHELLY
                battle("victory", team("SHELLY", "SHELLY", "SPIKE"), ENEMIES)); // SHELLY twice
    }

    private static List<Battle> counterBattles() {
        return List.of(
                battle("victory", team("COLT", "SHELLY", "SPIKE"), team("BROCK", "CARL", "BYRON")),
                battle("defeat", team("COLT", "AMBER", "SPIKE"), team("BROCK", "DARRYL", "BYRON")),
                battle("victory", team("COLT", "SHELLY", "SPIKE"), team("CARL", "DARRYL", "AMBER")));
    }

    private static String[] team(String... names) {
        return names;
    }

    private static Battle battle(String result, String[] allies, String[] enemies) {
        Battle battle = new Battle();
        battle.setBattleResult(result);
        battle.setAlly1Brawler(at(allies, 0));
        battle.setAlly2Brawler(at(allies, 1));
        battle.setAlly3Brawler(at(allies, 2));
        battle.setEnemy1Brawler(at(enemies, 0));
        battle.setEnemy2Brawler(at(enemies, 1));
        battle.setEnemy3Brawler(at(enemies, 2));
        return battle;
    }

    private static String at(String[] names, int index) {
        return index < names.length ? names[index] : null;
    }
}