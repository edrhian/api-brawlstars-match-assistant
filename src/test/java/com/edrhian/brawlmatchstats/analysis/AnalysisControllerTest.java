package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void allyBrawlersReadsAllTheQueryParameters() throws Exception {
        when(analysisService.analyzeAllyBrawlers(any(), anyInt())).thenReturn(new BrawlerAnalysisResponse(
                StatsDto.of(6, 3, 1),
                List.of(BrawlerStatsDto.of("COLT", 4, 1, 0))));

        mockMvc.perform(get("/analysis/ally-brawlers")
                        .param("players", "2VU8PGCC", "p0pulrc")
                        .param("battleTypes", "soloRanked", "ranked")
                        .param("trophiesMin", "100000")
                        .param("from", "2026-10-01")
                        .param("limit", "50")
                        .param("minGames", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall.games").value(10))
                .andExpect(jsonPath("$.overall.winrate").value(60.0))
                .andExpect(jsonPath("$.brawlers[0].brawler").value("COLT"))
                .andExpect(jsonPath("$.brawlers[0].winrate").value(80.0))
                .andExpect(jsonPath("$.brawlers[0].confidence").value("INSUFFICIENT"));

        BattleFilter expected = new BattleFilter(
                List.of("#2VU8PGCC", "#P0PULRC"),
                List.of(BattleType.soloRanked, BattleType.ranked),
                List.of(), 100000, null, List.of(), List.of(),
                "20261001T000000.000Z", null, 50);
        verify(analysisService).analyzeAllyBrawlers(expected, 5);
    }

    @Test
    void enemyBrawlersReturnsTheAnalysis() throws Exception {
        when(analysisService.analyzeEnemyBrawlers(any(), anyInt())).thenReturn(new BrawlerAnalysisResponse(
                StatsDto.of(1, 1, 0),
                List.of(BrawlerStatsDto.of("BROCK", 1, 1, 0))));

        mockMvc.perform(get("/analysis/enemy-brawlers").param("players", "2VU8PGCC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brawlers[0].brawler").value("BROCK"))
                .andExpect(jsonPath("$.brawlers[0].loserate").value(50.0));

        // Without other parameters: no filters and minGames 1
        verify(analysisService).analyzeEnemyBrawlers(
                new BattleFilter(List.of("#2VU8PGCC"), List.of(), List.of(), null, null,
                        List.of(), List.of(), null, null, null), 1);
    }

    @Test
    void returns400WhenThereAreNoPlayers() throws Exception {
        mockMvc.perform(get("/analysis/ally-brawlers"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("player")));

        verifyNoInteractions(analysisService);
    }

    @Test
    void returns400WhenBattleTypeIsUnknown() throws Exception {
        mockMvc.perform(get("/analysis/ally-brawlers")
                        .param("players", "2VU8PGCC")
                        .param("battleTypes", "trophyRoad"))
                .andExpect(status().isBadRequest());

        verify(analysisService, never()).analyzeAllyBrawlers(any(), anyInt());
    }

    @Test
    void returns400WhenDateIsInvalid() throws Exception {
        mockMvc.perform(get("/analysis/enemy-brawlers")
                        .param("players", "2VU8PGCC")
                        .param("from", "yesterday"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenAPlayerIsNotRegistered() throws Exception {
        when(analysisService.analyzeAllyBrawlers(any(), anyInt()))
                .thenThrow(new NoSuchElementException("Players not registered: #2VU8PGCC"));

        mockMvc.perform(get("/analysis/ally-brawlers").param("players", "2VU8PGCC"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Players not registered: #2VU8PGCC"));
    }

    @Test
    void filterOptionsReturnsTheExistingValues() throws Exception {
        when(analysisService.getFilterOptions(List.of("2VU8PGCC"))).thenReturn(new FilterOptionsResponse(
                List.of(BattleType.soloRanked), List.of("LEGENDARY I"), List.of("heist"),
                List.of("Kaboom Canyon"), 100000, 120000, "2026-10-01T10:00:00", "2026-10-07T12:00:00"));

        mockMvc.perform(get("/analysis/filter-options").param("players", "2VU8PGCC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battleTypes[0]").value("soloRanked"))
                .andExpect(jsonPath("$.ranks[0]").value("LEGENDARY I"))
                .andExpect(jsonPath("$.maps[0]").value("Kaboom Canyon"))
                .andExpect(jsonPath("$.trophiesMax").value(120000))
                .andExpect(jsonPath("$.lastBattle").value("2026-10-07T12:00:00"));
    }

    @Test
    void filterOptionsReturns400WithoutPlayers() throws Exception {
        mockMvc.perform(get("/analysis/filter-options"))
                .andExpect(status().isBadRequest());
    }
}