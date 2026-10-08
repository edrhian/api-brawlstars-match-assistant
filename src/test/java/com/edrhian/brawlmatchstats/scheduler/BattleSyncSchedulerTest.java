package com.edrhian.brawlmatchstats.scheduler;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BattleSyncSchedulerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private BattleService battleService;

    private BattleSyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new BattleSyncScheduler(playerService, battleService);
    }

    @Test
    void refreshesPlayerThenStoresBattlesForEachTrackedPlayer() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));

        scheduler.syncTrackedPlayers();

        InOrder order = inOrder(playerService, battleService);
        order.verify(playerService).refreshPlayer("#AAA");
        order.verify(battleService).fetchAndStoreBattles("#AAA");
        order.verify(playerService).refreshPlayer("#BBB");
        order.verify(battleService).fetchAndStoreBattles("#BBB");
    }

    @Test
    void continuesWithNextPlayerWhenOneFails() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(playerService.refreshPlayer("#AAA")).thenThrow(new BrawlStarsApiException(404, "Player not found"));

        scheduler.syncTrackedPlayers();

        verify(battleService, never()).fetchAndStoreBattles("#AAA");
        verify(battleService).fetchAndStoreBattles("#BBB");
    }

    @Test
    void continuesWithNextPlayerWhenUnexpectedErrorHappens() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(battleService.fetchAndStoreBattles("#AAA")).thenThrow(new IllegalStateException("boom"));

        scheduler.syncTrackedPlayers();

        verify(battleService).fetchAndStoreBattles("#BBB");
    }

    @Test
    void stopsWhenApiKeyIsRejected() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(playerService.refreshPlayer("#AAA")).thenThrow(new BrawlStarsApiException(403, "Invalid key"));

        scheduler.syncTrackedPlayers();

        verify(playerService, never()).refreshPlayer("#BBB");
        verify(battleService, never()).fetchAndStoreBattles(any());
    }

    @Test
    void stopsWhenRateLimitIsReached() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of(player("#AAA"), player("#BBB")));
        when(battleService.fetchAndStoreBattles("#AAA")).thenThrow(new BrawlStarsApiException(429, "Too many requests"));

        scheduler.syncTrackedPlayers();

        verify(playerService, never()).refreshPlayer("#BBB");
    }

    @Test
    void doesNothingWhenThereAreNoTrackedPlayers() {
        when(playerService.getTrackedPlayers()).thenReturn(List.of());

        scheduler.syncTrackedPlayers();

        verify(playerService, never()).refreshPlayer(any());
        verifyNoInteractions(battleService);
    }

    private static PlayerDto player(String tag) {
        return new PlayerDto(tag, "name", 1000, "LEGENDARY I", true,
                List.of(BattleType.ranked), "2026-10-07T11:00:39");
    }
}