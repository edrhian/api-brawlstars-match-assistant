package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrawlerStatsCalculatorTest {

    @Test
    void countsEachAllyBrawlerOncePerBattle() {
        List<Battle> battles = List.of(
                battle("victory", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"),
                // COLT is twice in this team, but it counts once
                battle("defeat", "COLT", "COLT", "AMBER", "BROCK", "CARL", "BYRON"));

        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.allyBrawlers(battles, 1);

        // Most played first, then alphabetical
        assertThat(stats).extracting(BrawlerStatsDto::brawler)
                .containsExactly("COLT", "AMBER", "SHELLY", "SPIKE");
        BrawlerStatsDto colt = stats.get(0);
        assertThat(colt.games()).isEqualTo(2);
        assertThat(colt.wins()).isEqualTo(1);
        assertThat(colt.losses()).isEqualTo(1);
        assertThat(colt.winrate()).isEqualTo(50.0);
    }

    @Test
    void countsDrawsAsGamesButNotWins() {
        List<Battle> battles = List.of(battle("draw", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"));

        BrawlerStatsDto colt = BrawlerStatsCalculator.allyBrawlers(battles, 1).get(0);

        assertThat(colt.games()).isEqualTo(1);
        assertThat(colt.draws()).isEqualTo(1);
        assertThat(colt.winrate()).isZero();
        assertThat(colt.loserate()).isZero();
    }

    @Test
    void ignoresBattlesWithoutKnownResult() {
        List<Battle> battles = List.of(
                battle(null, "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"),
                battle("something", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"));

        assertThat(BrawlerStatsCalculator.allyBrawlers(battles, 1)).isEmpty();
        assertThat(BrawlerStatsCalculator.overall(battles).games()).isZero();
    }

    @Test
    void ignoresMissingBrawlers() {
        List<Battle> battles = List.of(battle("victory", "COLT", null, null, "BROCK", null, null));

        assertThat(BrawlerStatsCalculator.allyBrawlers(battles, 1)).extracting(BrawlerStatsDto::brawler)
                .containsExactly("COLT");
        assertThat(BrawlerStatsCalculator.enemyBrawlers(battles, 1)).extracting(BrawlerStatsDto::brawler)
                .containsExactly("BROCK");
    }

    @Test
    void hidesBrawlersBelowMinGames() {
        List<Battle> battles = List.of(
                battle("victory", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"),
                battle("defeat", "COLT", "AMBER", "SPIKE", "BROCK", "CARL", "BYRON"));

        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.allyBrawlers(battles, 2);

        assertThat(stats).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
    }

    @Test
    void enemyStatsUseTheResultOfTheAllyTeam() {
        List<Battle> battles = List.of(
                battle("victory", "COLT", "SHELLY", "SPIKE", "BROCK", "CARL", "BYRON"),
                battle("defeat", "COLT", "SHELLY", "SPIKE", "BROCK", "DARRYL", "AMBER"));

        List<BrawlerStatsDto> stats = BrawlerStatsCalculator.enemyBrawlers(battles, 1);

        BrawlerStatsDto brock = stats.get(0);
        assertThat(brock.brawler()).isEqualTo("BROCK");
        assertThat(brock.games()).isEqualTo(2);
        assertThat(brock.winrate()).isEqualTo(50.0);
        assertThat(brock.loserate()).isEqualTo(50.0);
    }

    @Test
    void overallCountsAllBattlesWithAResult() {
        List<Battle> battles = List.of(
                battle("victory", "COLT", null, null, null, null, null),
                battle("defeat", "COLT", null, null, null, null, null),
                battle("draw", "COLT", null, null, null, null, null),
                battle(null, "COLT", null, null, null, null, null));

        StatsDto overall = BrawlerStatsCalculator.overall(battles);

        assertThat(overall.games()).isEqualTo(3);
        assertThat(overall.wins()).isEqualTo(1);
        assertThat(overall.losses()).isEqualTo(1);
        assertThat(overall.draws()).isEqualTo(1);
    }

    private static Battle battle(String result, String ally1, String ally2, String ally3,
                                 String enemy1, String enemy2, String enemy3) {
        Battle battle = new Battle();
        battle.setBattleResult(result);
        battle.setAlly1Brawler(ally1);
        battle.setAlly2Brawler(ally2);
        battle.setAlly3Brawler(ally3);
        battle.setEnemy1Brawler(enemy1);
        battle.setEnemy2Brawler(enemy2);
        battle.setEnemy3Brawler(enemy3);
        return battle;
    }
}