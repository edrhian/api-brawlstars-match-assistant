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
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    void brawlersReadsAllTheQueryParameters() throws Exception {
        when(analysisService.analyzeBrawlers(any(), any(), anyInt())).thenReturn(new BrawlerAnalysisResponse(
                10, List.of(BrawlerStatsDto.of("COLT", 4, 1, 0))));

        mockMvc.perform(get("/analysis/brawlers")
                        .param("players", "2VU8PGCC", "p0pulrc")
                        .param("battleTypes", "soloRanked", "ranked")
                        .param("trophiesMin", "100000")
                        .param("from", "2026-10-01")
                        .param("limit", "50")
                        .param("minGames", "5")
                        .param("with", "shelly")
                        .param("against", "BROCK", "CARL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battles").value(10))
                .andExpect(jsonPath("$.brawlers[0].brawler").value("COLT"))
                .andExpect(jsonPath("$.brawlers[0].games").value(5))
                .andExpect(jsonPath("$.brawlers[0].winrate").value(80.0))
                .andExpect(jsonPath("$.brawlers[0].confidence").value("INSUFFICIENT"));

        BattleFilter expectedFilter = new BattleFilter(
                List.of("#2VU8PGCC", "#P0PULRC"), false,
                List.of(BattleType.soloRanked, BattleType.ranked),
                List.of(), 100000, null, List.of(), List.of(),
                "20261001T000000.000Z", null, 50);
        BrawlerInput expectedInput = new BrawlerInput(List.of("SHELLY"), List.of("BROCK", "CARL"));
        verify(analysisService).analyzeBrawlers(expectedFilter, expectedInput, 5);
    }

    @Test
    void allPlayersNeedsNoTags() throws Exception {
        when(analysisService.analyzeBrawlers(any(), any(), anyInt()))
                .thenReturn(new BrawlerAnalysisResponse(0, List.of()));

        mockMvc.perform(get("/analysis/brawlers").param("allPlayers", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battles").value(0));

        // Without other parameters: no filters, no with/against and minGames 1
        verify(analysisService).analyzeBrawlers(
                new BattleFilter(List.of(), true, List.of(), List.of(), null, null,
                        List.of(), List.of(), null, null, null),
                BrawlerInput.NONE, 1);
    }

    @Test
    void returns400WhenThereAreNoPlayers() throws Exception {
        mockMvc.perform(get("/analysis/brawlers"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("player")));

        verifyNoInteractions(analysisService);
    }

    @Test
    void returns400WhenPlayersAndAllPlayersAreBothGiven() throws Exception {
        mockMvc.perform(get("/analysis/brawlers").param("players", "2VU8PGCC").param("allPlayers", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analysisService);
    }

    @Test
    void returns400WhenBattleTypeIsUnknown() throws Exception {
        mockMvc.perform(get("/analysis/brawlers")
                        .param("players", "2VU8PGCC")
                        .param("battleTypes", "trophyRoad"))
                .andExpect(status().isBadRequest());

        verify(analysisService, never()).analyzeBrawlers(any(), any(), anyInt());
    }

    @Test
    void returns400WhenDateIsInvalid() throws Exception {
        mockMvc.perform(get("/analysis/brawlers")
                        .param("allPlayers", "true")
                        .param("from", "yesterday"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenThereAreTooManyWithBrawlers() throws Exception {
        mockMvc.perform(get("/analysis/brawlers")
                        .param("allPlayers", "true")
                        .param("with", "SHELLY", "COLT", "SPIKE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("with")));
    }

    @Test
    void returns404WhenAPlayerIsNotRegistered() throws Exception {
        when(analysisService.analyzeBrawlers(any(), any(), anyInt()))
                .thenThrow(new NoSuchElementException("Players not registered: #2VU8PGCC"));

        mockMvc.perform(get("/analysis/brawlers").param("players", "2VU8PGCC"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Players not registered: #2VU8PGCC"));
    }

    @Test
    void theOldEndpointsDoNotExistAnymore() throws Exception {
        for (String path : List.of("ally-brawlers", "enemy-brawlers", "ally-synergy", "enemy-counters")) {
            mockMvc.perform(get("/analysis/" + path).param("players", "2VU8PGCC"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void filterOptionsReturnsTheExistingValues() throws Exception {
        when(analysisService.getFilterOptions(List.of("#2VU8PGCC"), false)).thenReturn(new FilterOptionsResponse(
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
    void filterOptionsAcceptsAllPlayers() throws Exception {
        when(analysisService.getFilterOptions(List.of(), true)).thenReturn(new FilterOptionsResponse(
                List.of(BattleType.ranked), List.of(), List.of(), List.of(), null, null, null, null));

        mockMvc.perform(get("/analysis/filter-options").param("allPlayers", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battleTypes[0]").value("ranked"));
    }

    @Test
    void filterOptionsReturns400WithoutPlayers() throws Exception {
        mockMvc.perform(get("/analysis/filter-options"))
                .andExpect(status().isBadRequest());

        verify(analysisService, never()).getFilterOptions(any(), anyBoolean());
    }
}