package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BanStatsCalculatorTest {

    @Test
    void countsBansAndBannedBattles() {
        List<Battle> battles = List.of(
                // MELODIE is banned by two players in the same battle
                battle("MELODIE", "VINCE", null, "MELODIE", null, null),
                battle("MELODIE", null, null, null, null, null),
                // A battle without bans does not count for the banrate
                battle(null, null, null, null, null, null));

        BanAnalysisResponse response = BanStatsCalculator.bans(battles);

        assertThat(response.battles()).isEqualTo(3);
        assertThat(response.battlesWithBans()).isEqualTo(2);
        assertThat(response.bans()).extracting(BanStatsDto::brawler).containsExactly("MELODIE", "VINCE");

        BanStatsDto melodie = response.bans().get(0);
        assertThat(melodie.bans()).isEqualTo(3);
        assertThat(melodie.bannedBattles()).isEqualTo(2);
        assertThat(melodie.banrate()).isEqualTo(100.0);

        BanStatsDto vince = response.bans().get(1);
        assertThat(vince.bans()).isEqualTo(1);
        assertThat(vince.bannedBattles()).isEqualTo(1);
        assertThat(vince.banrate()).isEqualTo(50.0);
    }

    @Test
    void sortsByBannedBattlesThenBansThenName() {
        List<Battle> battles = List.of(
                battle("COLT", "BROCK", "BROCK", "AMBER", null, null),
                battle("COLT", null, null, null, null, "DARRYL"));

        BanAnalysisResponse response = BanStatsCalculator.bans(battles);

        // COLT: 2 battles. BROCK: 1 battle but 2 bans. AMBER and DARRYL: 1 battle and 1 ban, so by name
        assertThat(response.bans()).extracting(BanStatsDto::brawler)
                .containsExactly("COLT", "BROCK", "AMBER", "DARRYL");
    }

    @Test
    void ignoresBlankBanNames() {
        List<Battle> battles = List.of(battle("", " ", null, null, null, null));

        BanAnalysisResponse response = BanStatsCalculator.bans(battles);

        assertThat(response.battles()).isEqualTo(1);
        assertThat(response.battlesWithBans()).isZero();
        assertThat(response.bans()).isEmpty();
    }

    @Test
    void confidenceDependsOnTheBattlesWithBans() {
        assertThat(BanStatsCalculator.bans(battlesWithOneBan(9)).confidence()).isEqualTo(Confidence.INSUFFICIENT);
        assertThat(BanStatsCalculator.bans(battlesWithOneBan(10)).confidence()).isEqualTo(Confidence.LOW);
    }

    @Test
    void noBattlesGivesAnEmptyResponse() {
        BanAnalysisResponse response = BanStatsCalculator.bans(List.of());

        assertThat(response.battles()).isZero();
        assertThat(response.battlesWithBans()).isZero();
        assertThat(response.confidence()).isEqualTo(Confidence.INSUFFICIENT);
        assertThat(response.bans()).isEmpty();
    }

    // ---------- Helpers ----------

    private static List<Battle> battlesWithOneBan(int count) {
        List<Battle> battles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            battles.add(battle("COLT", null, null, null, null, null));
        }
        return battles;
    }

    // The bans in the order of the slots: ally 1, 2, 3 and enemy 1, 2, 3
    private static Battle battle(String a1, String a2, String a3, String e1, String e2, String e3) {
        Battle battle = new Battle();
        battle.setAlly1Ban(a1);
        battle.setAlly2Ban(a2);
        battle.setAlly3Ban(a3);
        battle.setEnemy1Ban(e1);
        battle.setEnemy2Ban(e2);
        battle.setEnemy3Ban(e3);
        return battle;
    }
}