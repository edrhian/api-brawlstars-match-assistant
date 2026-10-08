package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsClient;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsPlayerResponse;
import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerServiceTest {

    private static final String TAG = "#2VU8PGCC";
    private static final String TIME_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private BrawlStarsClient brawlStarsClient;

    private PlayerService service;

    @BeforeEach
    void setUp() {
        service = new PlayerService(playerRepository, brawlStarsClient);
        // save() returns the same player it receives
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registersNewPlayerWithDataFromApi() {
        when(playerRepository.findById(TAG)).thenReturn(Optional.empty());
        when(brawlStarsClient.getPlayer(TAG))
                .thenReturn(new BrawlStarsPlayerResponse(TAG, "edrhian27", 111557, "LEGENDARY I"));

        // Lowercase tag without "#" and a repeated type
        PlayerDto dto = service.registerPlayer("2vu8pgcc",
                List.of(BattleType.ranked, BattleType.soloRanked, BattleType.ranked));

        assertThat(dto.tag()).isEqualTo(TAG);
        assertThat(dto.name()).isEqualTo("edrhian27");
        assertThat(dto.trophies()).isEqualTo(111557);
        assertThat(dto.rankedRank()).isEqualTo("LEGENDARY I");
        assertThat(dto.trackingEnabled()).isTrue();
        assertThat(dto.trackingTypes()).containsExactly(BattleType.ranked, BattleType.soloRanked);
        assertThat(dto.lastUpdated()).matches(TIME_FORMAT);
    }

    @Test
    void refreshUpdatesNameTrophiesAndRank() {
        Player existing = player(true, BattleType.ranked);
        when(playerRepository.findById(TAG)).thenReturn(Optional.of(existing));
        when(brawlStarsClient.getPlayer(TAG))
                .thenReturn(new BrawlStarsPlayerResponse(TAG, "newName", 120000, "MASTERS I"));

        PlayerDto dto = service.refreshPlayer(TAG);

        assertThat(dto.name()).isEqualTo("newName");
        assertThat(dto.trophies()).isEqualTo(120000);
        assertThat(dto.rankedRank()).isEqualTo("MASTERS I");
        assertThat(dto.lastUpdated()).matches(TIME_FORMAT);
        // Tracking settings are not touched by a refresh
        assertThat(dto.trackingEnabled()).isTrue();
        assertThat(dto.trackingTypes()).containsExactly(BattleType.ranked);
    }

    @Test
    void refreshThrowsWhenPlayerIsNotRegistered() {
        when(playerRepository.findById(TAG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refreshPlayer(TAG))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(TAG);
    }

    @Test
    void updatesTracking() {
        when(playerRepository.findById(TAG)).thenReturn(Optional.of(player(true, BattleType.ranked)));

        PlayerDto dto = service.updateTracking(TAG, false, List.of(BattleType.friendly));

        assertThat(dto.trackingEnabled()).isFalse();
        assertThat(dto.trackingTypes()).containsExactly(BattleType.friendly);
    }

    @Test
    void returnsOnlyTrackedPlayers() {
        when(playerRepository.findByTrackingEnabledTrue()).thenReturn(List.of(player(true, BattleType.ranked)));

        List<PlayerDto> tracked = service.getTrackedPlayers();

        assertThat(tracked).extracting(PlayerDto::tag).containsExactly(TAG);
    }

    private Player player(boolean trackingEnabled, BattleType... types) {
        Player player = new Player();
        player.setTag(TAG);
        player.setName("edrhian27");
        player.setTrophies(111557);
        player.setRankedRank("LEGENDARY I");
        player.setTrackingEnabled(trackingEnabled);
        player.setTrackingTypes(new ArrayList<>(List.of(types)));
        player.setLastUpdated("2026-10-07T11:00:39");
        return player;
    }
}