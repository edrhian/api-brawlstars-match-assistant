package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import com.edrhian.brawlmatchstats.battle.BattleRepository;
import com.edrhian.brawlmatchstats.player.Player;
import com.edrhian.brawlmatchstats.player.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalysisServiceInputTest {

    private static final BattleFilter FILTER = new BattleFilter(
            List.of("#A"), false, List.of(), List.of(), null, null, List.of(), List.of(), null, null, null);

    @Mock
    private BattleRepository battleRepository;

    @Mock
    private PlayerRepository playerRepository;

    private AnalysisService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        service = new AnalysisService(battleRepository, playerRepository);

        Player player = new Player();
        player.setTag("#A");
        when(playerRepository.findAllById(any())).thenReturn(List.of(player));

        when(battleRepository.findAll(any(Specification.class))).thenReturn(List.of(
                battle("victory", "SHELLY", "COLT", "SPIKE", "BROCK", "CARL", "BYRON"),
                battle("defeat", "COLT", "AMBER", "SPIKE", "CARL", "DARRYL", "BYRON")));
    }

    @Test
    void allySynergyComparesTheInputBattlesWithTheRest() {
        InputAnalysisResponse response = service.analyzeAllySynergy(FILTER, List.of("SHELLY"), 1);

        assertThat(response.inputBrawlers()).containsExactly("SHELLY");
        assertThat(response.overall().games()).isEqualTo(2);
        assertThat(response.withInput().games()).isEqualTo(1);
        assertThat(response.withInput().winrate()).isEqualTo(100.0);
        assertThat(response.brawlers()).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
    }

    @Test
    void enemyCountersComparesTheInputBattlesWithTheRest() {
        InputAnalysisResponse response = service.analyzeEnemyCounters(FILTER, List.of("BROCK"), 1);

        assertThat(response.inputBrawlers()).containsExactly("BROCK");
        assertThat(response.overall().games()).isEqualTo(2);
        // Only the first battle has BROCK in the enemy team
        assertThat(response.withInput().games()).isEqualTo(1);
        assertThat(response.brawlers()).extracting(BrawlerStatsDto::brawler)
                .containsExactly("COLT", "SHELLY", "SPIKE");
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