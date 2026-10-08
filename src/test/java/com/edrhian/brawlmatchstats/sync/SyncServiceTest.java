package com.edrhian.brawlmatchstats.sync;

import com.edrhian.brawlmatchstats.battle.BattleService;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsApiException;
import com.edrhian.brawlmatchstats.player.PlayerDto;
import com.edrhian.brawlmatchstats.player.PlayerService;
import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SyncServiceTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private BattleService battleService;

    private SyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SyncService(playerService, battleService);
    }

    // ---------- One player ----------

    @Test
    void syncsOnePlayer() {
        PlayerDto refreshed = player("#AAA");
        when(playerService.refreshPlayer("#AAA")).thenReturn(refreshed);
        when(battleService.fetchAndStoreBattles("#AAA")).thenReturn(3);

        PlayerSyncResult result = syncService.syncPlayer("#AAA");

        assertThat(result).isEqualTo(new PlayerSyncResult(refreshed, 3));
    }

    @Test
    void syncOnePlayerThrowsTheError() {
        when(playerService.refreshPlayer("#AAA")).thenThrow(new BrawlStarsApiException(404, "Player not found"));

        assertThatThrownBy(() -> syncService.syncPlayer("#AAA"))
                .isInstanceOf(BrawlStarsApiException.class);
        verify(battleService, never()).fetchAndStoreBattles(any());
    }

    // ---------- All tracked players ----------

    @Test
    void refreshesPlayerThenStoresBattlesForEachTrackedPlayer() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(battleService.fetchAndStoreBattles("#AAA")).thenReturn(1);
        when(battleService.fetchAndStoreBattles("#BBB")).thenReturn(4);

        SyncSummary summary = syncService.syncTrackedPlayers();

        InOrder order = inOrder(playerService, battleService);
        order.verify(playerService).refreshPlayer("#AAA");
        order.verify(battleService).fetchAndStoreBattles("#AAA");
        order.verify(playerService).refreshPlayer("#BBB");
        order.verify(battleService).fetchAndStoreBattles("#BBB");
        assertThat(summary).isEqualTo(new SyncSummary(2, 2, 0, 5, false));
    }

    @Test
    void continuesWithNextPlayerWhenOneFails() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(playerService.refreshPlayer("#AAA")).thenThrow(new BrawlStarsApiException(404, "Player not found"));
        when(battleService.fetchAndStoreBattles("#BBB")).thenReturn(2);

        SyncSummary summary = syncService.syncTrackedPlayers();

        verify(battleService, never()).fetchAndStoreBattles("#AAA");
        verify(battleService).fetchAndStoreBattles("#BBB");
        assertThat(summary).isEqualTo(new SyncSummary(2, 1, 1, 2, false));
    }

    @Test
    void continuesWithNextPlayerWhenUnexpectedErrorHappens() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(battleService.fetchAndStoreBattles("#AAA")).thenThrow(new IllegalStateException("boom"));

        SyncSummary summary = syncService.syncTrackedPlayers();

        verify(battleService).fetchAndStoreBattles("#BBB");
        assertThat(summary).isEqualTo(new SyncSummary(2, 1, 1, 0, false));
    }

    @Test
    void stopsWhenApiKeyIsRejected() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(playerService.refreshPlayer("#AAA")).thenThrow(new BrawlStarsApiException(403, "Invalid key"));

        SyncSummary summary = syncService.syncTrackedPlayers();

        verify(playerService, never()).refreshPlayer("#BBB");
        verify(battleService, never()).fetchAndStoreBattles(any());
        assertThat(summary).isEqualTo(new SyncSummary(2, 0, 1, 0, true));
    }

    @Test
    void stopsWhenRateLimitIsReached() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(battleService.fetchAndStoreBattles("#AAA")).thenThrow(new BrawlStarsApiException(429, "Too many requests"));

        SyncSummary summary = syncService.syncTrackedPlayers();

        verify(playerService, never()).refreshPlayer("#BBB");
        assertThat(summary).isEqualTo(new SyncSummary(2, 0, 1, 0, true));
    }

    @Test
    void doesNothingWhenThereAreNoTrackedPlayers() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of());

        SyncSummary summary = syncService.syncTrackedPlayers();

        verify(playerService, never()).refreshPlayer(any());
        assertThat(summary).isEqualTo(new SyncSummary(0, 0, 0, 0, false));
    }

    private static PlayerDto player(String tag) {
        return new PlayerDto(tag, "name", 1000, "LEGENDARY I", true,
                List.of(BattleType.ranked), "2026-10-07T11:00:39");
    }
}