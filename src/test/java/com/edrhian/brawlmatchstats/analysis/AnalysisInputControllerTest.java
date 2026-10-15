package com.edrhian.brawlmatchstats.analysis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisInputControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void allySynergyReadsTheBrawlersAndTheFilters() throws Exception {
        when(analysisService.analyzeAllySynergy(any(), any(), anyInt())).thenReturn(new InputAnalysisResponse(
                List.of("SHELLY", "COLT"),
                StatsDto.of(6, 3, 1),
                StatsDto.of(3, 1, 0),
                List.of(BrawlerStatsDto.of("SPIKE", 2, 0, 0))));

        mockMvc.perform(get("/analysis/ally-synergy")
                        .param("players", "2VU8PGCC")
                        .param("brawlers", "shelly", "colt")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputBrawlers[0]").value("SHELLY"))
                .andExpect(jsonPath("$.overall.games").value(10))
                .andExpect(jsonPath("$.withInput.games").value(4))
                .andExpect(jsonPath("$.brawlers[0].brawler").value("SPIKE"))
                .andExpect(jsonPath("$.brawlers[0].winrate").value(100.0));

        BattleFilter expected = new BattleFilter(List.of("#2VU8PGCC"), false, List.of(), List.of(), null, null,
                List.of(), List.of(), null, null, 100);
        verify(analysisService).analyzeAllySynergy(expected, List.of("SHELLY", "COLT"), 1);
    }

    @Test
    void enemyCountersReadsTheBrawlersAndTheFilters() throws Exception {
        when(analysisService.analyzeEnemyCounters(any(), any(), anyInt())).thenReturn(new InputAnalysisResponse(
                List.of("BROCK"),
                StatsDto.of(6, 3, 1),
                StatsDto.of(1, 3, 0),
                List.of(BrawlerStatsDto.of("COLT", 1, 0, 0))));

        mockMvc.perform(get("/analysis/enemy-counters")
                        .param("players", "2VU8PGCC")
                        .param("brawlers", "BROCK")
                        .param("minGames", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputBrawlers[0]").value("BROCK"))
                .andExpect(jsonPath("$.withInput.winrate").value(25.0))
                .andExpect(jsonPath("$.brawlers[0].brawler").value("COLT"));

        verify(analysisService).analyzeEnemyCounters(
                new BattleFilter(List.of("#2VU8PGCC"), false, List.of(), List.of(), null, null,
                        List.of(), List.of(), null, null, null),
                List.of("BROCK"), 3);
    }

    @Test
    void returns400WhenThereAreNoBrawlers() throws Exception {
        mockMvc.perform(get("/analysis/ally-synergy").param("players", "2VU8PGCC"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/analysis/enemy-counters").param("players", "2VU8PGCC"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analysisService);
    }

    @Test
    void returns400WhenThereAreMoreThanThreeBrawlers() throws Exception {
        mockMvc.perform(get("/analysis/enemy-counters")
                        .param("players", "2VU8PGCC")
                        .param("brawlers", "A", "B", "C", "D"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("between 1 and 3")));

        verifyNoInteractions(analysisService);
    }

    @Test
    void returns404WhenAPlayerIsNotRegistered() throws Exception {
        when(analysisService.analyzeAllySynergy(any(), any(), anyInt()))
                .thenThrow(new NoSuchElementException("Players not registered: #2VU8PGCC"));

        mockMvc.perform(get("/analysis/ally-synergy")
                        .param("players", "2VU8PGCC")
                        .param("brawlers", "SHELLY"))
                .andExpect(status().isNotFound());
    }
}