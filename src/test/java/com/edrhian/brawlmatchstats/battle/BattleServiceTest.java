package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse.BattleDetails;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse.Brawler;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse.Event;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse.Item;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse.Participant;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsClient;
import com.edrhian.brawlmatchstats.player.Player;
import com.edrhian.brawlmatchstats.player.PlayerRepository;
import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BattleServiceTest {

    private static final String PLAYER_TAG = "#2VU8PGCC";
    private static final String MATE_TAG = "#P0R9Y9J09";
    private static final String ENEMY_TAG = "#9J8PRJRQL";
    private static final String BATTLE_TIME = "20261007T070930.000Z";

    // The smallest of the 6 tags of defaultTeams() (it belongs to an enemy), so it is part of the ID
    private static final String EXPECTED_ID = "20261007T070930.000Z_#2R0P8LPR9";

    @Mock
    private BattleRepository battleRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private BrawlStarsClient brawlStarsClient;

    private BattleService service;

    @BeforeEach
    void setUp() {
        service = new BattleService(battleRepository, playerRepository, brawlStarsClient);
    }

    // ---------- Saving ----------

    @Test
    void savesBattleFromTheTrackedPlayerPointOfView() {
        givenTrackedPlayer(true, BattleType.soloRanked);
        givenBattleLog(item("soloRanked", defaultTeams()));

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isEqualTo(1);
        Battle battle = savedBattle();
        assertThat(battle.getId()).isEqualTo(EXPECTED_ID);
        assertThat(battle.getBattleTime()).isEqualTo(BATTLE_TIME);
        assertThat(battle.getBattleType()).isEqualTo(BattleType.soloRanked);
        assertThat(battle.getGameMode()).isEqualTo("heist");
        assertThat(battle.getMap()).isEqualTo("Kaboom Canyon");
        assertThat(battle.getRank()).isEqualTo("LEGENDARY I");
        assertThat(battle.getTrophies()).isEqualTo(111557);
        assertThat(battle.getBattleResult()).isEqualTo("victory");
        assertThat(battle.getFetchedAt()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

        // The tracked player is the second one in the API team, but it must be Ally 1
        assertThat(battle.getAlly1()).isEqualTo(PLAYER_TAG);
        assertThat(battle.getAlly1Brawler()).isEqualTo("DARRYL");
        assertThat(battle.getAlly2()).isEqualTo(MATE_TAG);
        assertThat(battle.getAlly2Brawler()).isEqualTo("COLT");
        assertThat(battle.getAlly3()).isEqualTo("#P2J88GQG");
        assertThat(battle.getAlly3Brawler()).isEqualTo("AMBER");
        assertThat(battle.getEnemy1()).isEqualTo(ENEMY_TAG);
        assertThat(battle.getEnemy1Brawler()).isEqualTo("BROCK");
        assertThat(battle.getEnemy2()).isEqualTo("#2R0P8LPR9");
        assertThat(battle.getEnemy2Brawler()).isEqualTo("CARL");
        assertThat(battle.getEnemy3()).isEqualTo("#Y8Q8UPCGV");
        assertThat(battle.getEnemy3Brawler()).isEqualTo("BYRON");
    }

    @Test
    void usesTheTeamOfTheTrackedPlayerEvenWhenListedSecond() {
        givenTrackedPlayer(true, BattleType.soloRanked);
        List<List<Participant>> swapped = List.of(defaultTeams().get(1), defaultTeams().get(0));
        givenBattleLog(item("soloRanked", swapped));

        service.fetchAndStoreBattles(PLAYER_TAG);

        Battle battle = savedBattle();
        assertThat(battle.getAlly1()).isEqualTo(PLAYER_TAG);
        assertThat(battle.getAlly2()).isEqualTo(MATE_TAG);
        assertThat(battle.getEnemy1()).isEqualTo(ENEMY_TAG);
    }

    @Test
    void returnsHowManyBattlesWereStored() {
        givenTrackedPlayer(true, BattleType.soloRanked);
        givenBattleLog(item("soloRanked", defaultTeams()), item("trophyRoad", defaultTeams()));

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isEqualTo(1);
    }

    // ---------- Filters ----------

    @Test
    void skipsUnknownBattleTypesSilently() {
        givenTrackedPlayer(true, BattleType.ranked, BattleType.soloRanked, BattleType.friendly);
        givenBattleLog(item("trophyRoad", defaultTeams()), item("challenge", defaultTeams()));

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isZero();
        verify(battleRepository, never()).save(any());
    }

    @Test
    void skipsBattleTypeThePlayerDoesNotTrack() {
        givenTrackedPlayer(true, BattleType.soloRanked);
        givenBattleLog(item("ranked", defaultTeams()));

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isZero();
        verify(battleRepository, never()).save(any());
    }

    @Test
    void skipsBattleWithoutTeams() {
        givenTrackedPlayer(true, BattleType.soloRanked);
        givenBattleLog(item("soloRanked", null));

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isZero();
        verify(battleRepository, never()).save(any());
    }

    // ---------- Duplicates ----------

    @Test
    void skipsBattleAlreadyStored() {
        givenTrackedPlayer(true, BattleType.soloRanked);
        givenBattleLog(item("soloRanked", defaultTeams()));
        when(battleRepository.existsById(EXPECTED_ID)).thenReturn(true);

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isZero();
        verify(battleRepository, never()).save(any());
    }

    @Test
    void sameBattleGetsTheSameIdWhoeverReadsIt() {
        // The same battle read by a player of team 1 and by a player of the other team
        givenTrackedPlayer(true, BattleType.soloRanked);
        givenBattleLog(item("soloRanked", defaultTeams()));
        givenEnemyPlayerWithTheSameBattle();

        service.fetchAndStoreBattles(PLAYER_TAG);
        service.fetchAndStoreBattles(ENEMY_TAG);

        ArgumentCaptor<Battle> captor = ArgumentCaptor.forClass(Battle.class);
        verify(battleRepository, times(2)).save(captor.capture());
        List<Battle> saved = captor.getAllValues();

        assertThat(saved.get(0).getAlly1()).isEqualTo(PLAYER_TAG);
        assertThat(saved.get(1).getAlly1()).isEqualTo(ENEMY_TAG);
        assertThat(saved.get(0).getId()).isEqualTo(EXPECTED_ID);
        assertThat(saved.get(1).getId()).isEqualTo(EXPECTED_ID);
    }

    // ---------- Player checks ----------

    @Test
    void doesNothingWhenTrackingIsDisabled() {
        givenTrackedPlayer(false, BattleType.soloRanked);

        int saved = service.fetchAndStoreBattles(PLAYER_TAG);

        assertThat(saved).isZero();
        verifyNoInteractions(brawlStarsClient);
    }

    @Test
    void throwsWhenPlayerIsNotRegistered() {
        when(playerRepository.findById(PLAYER_TAG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.fetchAndStoreBattles(PLAYER_TAG))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(PLAYER_TAG);
    }

    // ---------- Helpers ----------

    private void givenTrackedPlayer(boolean trackingEnabled, BattleType... types) {
        Player player = new Player();
        player.setTag(PLAYER_TAG);
        player.setName("edrhian27");
        player.setTrophies(111557);
        player.setRankedRank("LEGENDARY I");
        player.setTrackingEnabled(trackingEnabled);
        player.setTrackingTypes(new ArrayList<>(List.of(types)));
        when(playerRepository.findById(PLAYER_TAG)).thenReturn(Optional.of(player));
    }

    // A second tracked player that is in the other team and sees the same battle in his log
    private void givenEnemyPlayerWithTheSameBattle() {
        Player enemy = new Player();
        enemy.setTag(ENEMY_TAG);
        enemy.setName("enemy");
        enemy.setTrackingEnabled(true);
        enemy.setTrackingTypes(new ArrayList<>(List.of(BattleType.soloRanked)));
        when(playerRepository.findById(ENEMY_TAG)).thenReturn(Optional.of(enemy));
        when(brawlStarsClient.getBattleLog(ENEMY_TAG)).thenReturn(List.of(item("soloRanked", defaultTeams())));
    }

    private void givenBattleLog(Item... items) {
        when(brawlStarsClient.getBattleLog(PLAYER_TAG)).thenReturn(List.of(items));
    }

    private Battle savedBattle() {
        ArgumentCaptor<Battle> captor = ArgumentCaptor.forClass(Battle.class);
        verify(battleRepository).save(captor.capture());
        return captor.getValue();
    }

    private static Item item(String type, List<List<Participant>> teams) {
        return new Item(BATTLE_TIME,
                new Event("heist", "Kaboom Canyon"),
                new BattleDetails("heist", type, "victory", teams));
    }

    // The tracked player is in the middle of the first team, like in the real battle log sample
    private static List<List<Participant>> defaultTeams() {
        return List.of(
                List.of(participant(MATE_TAG, "COLT"),
                        participant(PLAYER_TAG, "DARRYL"),
                        participant("#P2J88GQG", "AMBER")),
                List.of(participant(ENEMY_TAG, "BROCK"),
                        participant("#2R0P8LPR9", "CARL"),
                        participant("#Y8Q8UPCGV", "BYRON")));
    }

    private static Participant participant(String tag, String brawler) {
        return new Participant(tag, "name", new Brawler(brawler));
    }
}