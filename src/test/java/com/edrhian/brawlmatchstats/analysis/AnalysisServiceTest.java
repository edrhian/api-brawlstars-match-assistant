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
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalysisServiceTest {

    private static final BattleFilter FILTER = new BattleFilter(
            List.of("#A"), false, List.of(), List.of(), null, null, List.of(), List.of(), null, null, null);

    private static final BattleFilter ALL_PLAYERS = new BattleFilter(
            List.of(), true, List.of(), List.of(), null, null, List.of(), List.of(), null, null, null);

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
    void everyBrawlerGetsTheResultOfItsOwnTeam() {
        BrawlerAnalysisResponse response = service.analyzeBrawlers(FILTER, BrawlerInput.NONE, 1);

        assertThat(response.battles()).isEqualTo(2);
        assertThat(response.brawlers()).extracting(BrawlerStatsDto::brawler).containsExactly(
                "BYRON", "CARL", "COLT", "SPIKE", "AMBER", "BROCK", "DARRYL", "SHELLY");
        assertThat(stat(response, "DARRYL").winrate()).isEqualTo(100.0);
        assertThat(stat(response, "BROCK").winrate()).isZero();
        assertThat(stat(response, "BYRON").winrate()).isEqualTo(50.0);
    }

    @Test
    void withAndAgainstNarrowDownTheBrawlers() {
        BrawlerAnalysisResponse withShelly = service.analyzeBrawlers(
                FILTER, new BrawlerInput(List.of("SHELLY"), List.of()), 1);
        BrawlerAnalysisResponse againstBrock = service.analyzeBrawlers(
                FILTER, new BrawlerInput(List.of(), List.of("BROCK")), 1);

        // "battles" is the number of filtered battles, before with and against
        assertThat(withShelly.battles()).isEqualTo(2);
        assertThat(withShelly.brawlers()).extracting(BrawlerStatsDto::brawler).containsExactly("COLT", "SPIKE");
        assertThat(againstBrock.brawlers()).extracting(BrawlerStatsDto::brawler)
                .containsExactly("COLT", "SHELLY", "SPIKE");
    }

    @Test
    void minGamesHidesBrawlersWithFewerGames() {
        BrawlerAnalysisResponse response = service.analyzeBrawlers(FILTER, BrawlerInput.NONE, 2);

        assertThat(response.brawlers()).extracting(BrawlerStatsDto::brawler)
                .containsExactly("BYRON", "CARL", "COLT", "SPIKE");
    }

    @Test
    void allPlayersDoesNotCheckRegisteredPlayers() {
        BrawlerAnalysisResponse response = service.analyzeBrawlers(ALL_PLAYERS, BrawlerInput.NONE, 1);

        assertThat(response.battles()).isEqualTo(2);
        verify(playerRepository, never()).findAllById(any());
    }

    @Test
    void throwsWhenAPlayerIsNotRegistered() {
        when(playerRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.analyzeBrawlers(FILTER, BrawlerInput.NONE, 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("#A");
    }

    private static BrawlerStatsDto stat(BrawlerAnalysisResponse response, String brawler) {
        return response.brawlers().stream().filter(stat -> stat.brawler().equals(brawler)).findFirst().orElseThrow();
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