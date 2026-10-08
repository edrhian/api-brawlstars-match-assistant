package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsApiException;
import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    // ---------- POST /players/{tag} ----------

    @Test
    void addsPlayerWithDefaultsWhenThereIsNoBody() throws Exception {
        when(playerService.registerPlayer("2VU8PGCC", null, null))
                .thenReturn(dto(true, BattleType.ranked, BattleType.soloRanked, BattleType.friendly));

        mockMvc.perform(post("/players/2VU8PGCC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tag").value("#2VU8PGCC"))
                .andExpect(jsonPath("$.trackingEnabled").value(true))
                .andExpect(jsonPath("$.trackingTypes.length()").value(3));

        verify(playerService).registerPlayer("2VU8PGCC", null, null);
    }

    @Test
    void addsPlayerWithTheValuesOfTheBody() throws Exception {
        when(playerService.registerPlayer("2VU8PGCC", false, List.of(BattleType.ranked, BattleType.friendly)))
                .thenReturn(dto(false, BattleType.ranked, BattleType.friendly));

        mockMvc.perform(post("/players/2VU8PGCC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingEnabled\":false,\"trackingTypes\":[\"ranked\",\"friendly\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingEnabled").value(false))
                .andExpect(jsonPath("$.trackingTypes[1]").value("friendly"));
    }

    @Test
    void addPlayerReturns409WhenAlreadyExists() throws Exception {
        when(playerService.registerPlayer("2VU8PGCC", null, null))
                .thenThrow(new PlayerAlreadyExistsException("#2VU8PGCC"));

        mockMvc.perform(post("/players/2VU8PGCC"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Player already registered: #2VU8PGCC"));
    }

    @Test
    void addPlayerReturns404WhenTagDoesNotExistInBrawlStars() throws Exception {
        when(playerService.registerPlayer("NOPLAYER", null, null))
                .thenThrow(new BrawlStarsApiException(404, "Player not found"));

        mockMvc.perform(post("/players/NOPLAYER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPlayerReturns502WhenBrawlStarsRejectsTheKey() throws Exception {
        when(playerService.registerPlayer("2VU8PGCC", null, null))
                .thenThrow(new BrawlStarsApiException(403, "Invalid key"));

        mockMvc.perform(post("/players/2VU8PGCC"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void addPlayerReturns400WhenTypesAreEmpty() throws Exception {
        mockMvc.perform(post("/players/2VU8PGCC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingTypes\":[]}"))
                .andExpect(status().isBadRequest());

        verify(playerService, never()).registerPlayer(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addPlayerReturns400WhenTypeIsUnknown() throws Exception {
        mockMvc.perform(post("/players/2VU8PGCC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingTypes\":[\"trophyRoad\"]}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET ----------

    @Test
    void getsAllPlayers() throws Exception {
        when(playerService.getAllPlayers()).thenReturn(List.of(dto(true, BattleType.ranked), dto(false)));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("edrhian27"));
    }

    @Test
    void getsOnePlayer() throws Exception {
        when(playerService.getPlayer("2VU8PGCC")).thenReturn(dto(true, BattleType.ranked));

        mockMvc.perform(get("/players/2VU8PGCC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("#2VU8PGCC"))
                .andExpect(jsonPath("$.rankedRank").value("LEGENDARY I"));
    }

    @Test
    void getPlayerReturns404WhenNotRegistered() throws Exception {
        when(playerService.getPlayer("2VU8PGCC"))
                .thenThrow(new NoSuchElementException("Player not registered: #2VU8PGCC"));

        mockMvc.perform(get("/players/2VU8PGCC"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Player not registered: #2VU8PGCC"));
    }

    // ---------- Tracking ----------

    @Test
    void enablesTracking() throws Exception {
        when(playerService.setTrackingEnabled("2VU8PGCC", true)).thenReturn(dto(true, BattleType.ranked));

        mockMvc.perform(post("/players/2VU8PGCC/tracking/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingEnabled").value(true));
    }

    @Test
    void disablesTracking() throws Exception {
        when(playerService.setTrackingEnabled("2VU8PGCC", false)).thenReturn(dto(false, BattleType.ranked));

        mockMvc.perform(post("/players/2VU8PGCC/tracking/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingEnabled").value(false));
    }

    @Test
    void changesTrackingTypes() throws Exception {
        when(playerService.setTrackingTypes("2VU8PGCC", List.of(BattleType.soloRanked)))
                .thenReturn(dto(true, BattleType.soloRanked));

        mockMvc.perform(put("/players/2VU8PGCC/tracking/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingTypes\":[\"soloRanked\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingTypes[0]").value("soloRanked"));
    }

    @Test
    void changeTrackingTypesReturns400WhenListIsEmptyOrMissing() throws Exception {
        mockMvc.perform(put("/players/2VU8PGCC/tracking/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingTypes\":[]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/players/2VU8PGCC/tracking/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeTrackingTypesReturns404WhenNotRegistered() throws Exception {
        when(playerService.setTrackingTypes("2VU8PGCC", List.of(BattleType.ranked)))
                .thenThrow(new NoSuchElementException("Player not registered: #2VU8PGCC"));

        mockMvc.perform(put("/players/2VU8PGCC/tracking/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingTypes\":[\"ranked\"]}"))
                .andExpect(status().isNotFound());
    }

    private static PlayerDto dto(boolean trackingEnabled, BattleType... types) {
        return new PlayerDto("#2VU8PGCC", "edrhian27", 111557, "LEGENDARY I",
                trackingEnabled, List.of(types), "2026-10-07T11:00:39");
    }
}