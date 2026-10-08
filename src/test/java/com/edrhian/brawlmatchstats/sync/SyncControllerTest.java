package com.edrhian.brawlmatchstats.sync;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsApiException;
import com.edrhian.brawlmatchstats.player.PlayerDto;
import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncController.class)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncService syncService;

    @Test
    void syncsAllTrackedPlayers() throws Exception {
        when(syncService.syncTrackedPlayers()).thenReturn(new SyncSummary(3, 2, 1, 5, false));

        mockMvc.perform(post("/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackedPlayers").value(3))
                .andExpect(jsonPath("$.syncedPlayers").value(2))
                .andExpect(jsonPath("$.failedPlayers").value(1))
                .andExpect(jsonPath("$.newBattles").value(5))
                .andExpect(jsonPath("$.stoppedEarly").value(false));
    }

    @Test
    void syncsOnePlayer() throws Exception {
        PlayerDto player = new PlayerDto("#2VU8PGCC", "edrhian27", 111557, "LEGENDARY I",
                true, List.of(BattleType.ranked), "2026-10-07T11:00:39");
        when(syncService.syncPlayer("2VU8PGCC")).thenReturn(new PlayerSyncResult(player, 4));

        mockMvc.perform(post("/players/2VU8PGCC/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.tag").value("#2VU8PGCC"))
                .andExpect(jsonPath("$.newBattles").value(4));
    }

    @Test
    void syncOnePlayerReturns404WhenNotRegistered() throws Exception {
        when(syncService.syncPlayer("2VU8PGCC"))
                .thenThrow(new NoSuchElementException("Player not registered: #2VU8PGCC"));

        mockMvc.perform(post("/players/2VU8PGCC/sync"))
                .andExpect(status().isNotFound());
    }

    @Test
    void syncOnePlayerReturns502WhenBrawlStarsIsUnavailable() throws Exception {
        when(syncService.syncPlayer("2VU8PGCC"))
                .thenThrow(new BrawlStarsApiException(503, "Maintenance"));

        mockMvc.perform(post("/players/2VU8PGCC/sync"))
                .andExpect(status().isBadGateway());
    }
}