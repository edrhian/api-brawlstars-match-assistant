package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import com.edrhian.brawlmatchstats.battle.BattleRepository;
import com.edrhian.brawlmatchstats.player.Player;
import com.edrhian.brawlmatchstats.player.PlayerRepository;
import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the filters against a real SQLite database.
 */
@DataJpaTest
class AnalysisQueriesTest {

    private static final String T1 = "20261001T100000.000Z";
    private static final String T2 = "20261001T110000.000Z";
    private static final String T3 = "20261001T120000.000Z";
    private static final String T4 = "20261001T130000.000Z";
    private static final String T5 = "20261001T140000.000Z";

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private AnalysisService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisService(battleRepository, playerRepository);
        registerPlayers("#A", "#B", "#C");
    }

    // ---------- Filters ----------

    @Test
    void matchesPlayerInAnyAllySlotButNotAsEnemy() {
        Battle asAlly1 = battle(T1, "#A");
        Battle asAlly2 = battle(T2, "#B");
        asAlly2.setAlly2("#A");
        Battle asAlly3 = battle(T3, "#C");
        asAlly3.setAlly3("#A");
        Battle asEnemy = battle(T4, "#C");
        asEnemy.setEnemy1("#A");
        battleRepository.saveAll(List.of(asAlly1, asAlly2, asAlly3, asEnemy));

        List<Battle> result = service.findBattles(Filter.forPlayers("#A").build());

        assertThat(result).extracting(Battle::getBattleTime).containsExactlyInAnyOrder(T1, T2, T3);
    }

    @Test
    void filtersByBattleType() {
        Battle solo = battle(T1, "#A");
        Battle ranked = battle(T2, "#A");
        ranked.setBattleType(BattleType.ranked);
        Battle friendly = battle(T3, "#A");
        friendly.setBattleType(BattleType.friendly);
        battleRepository.saveAll(List.of(solo, ranked, friendly));

        List<Battle> result = service.findBattles(
                Filter.forPlayers("#A").types(BattleType.soloRanked, BattleType.friendly).build());

        assertThat(result).extracting(Battle::getBattleTime).containsExactlyInAnyOrder(T1, T3);
    }

    @Test
    void filtersByTimeFrameIncludingLimits() {
        battleRepository.saveAll(List.of(battle(T1, "#A"), battle(T2, "#A"), battle(T3, "#A")));

        assertThat(service.findBattles(Filter.forPlayers("#A").time(T2, T3).build()))
                .extracting(Battle::getBattleTime).containsExactlyInAnyOrder(T2, T3);
        assertThat(service.findBattles(Filter.forPlayers("#A").time(null, T1).build()))
                .extracting(Battle::getBattleTime).containsExactly(T1);
        assertThat(service.findBattles(Filter.forPlayers("#A").time(T3, null).build()))
                .extracting(Battle::getBattleTime).containsExactly(T3);
    }

    @Test
    void filtersByTrophiesRange() {
        Battle low = battle(T1, "#A");
        low.setTrophies(90000);
        Battle middle = battle(T2, "#A");
        middle.setTrophies(100000);
        Battle high = battle(T3, "#A");
        high.setTrophies(110000);
        battleRepository.saveAll(List.of(low, middle, high));

        assertThat(service.findBattles(Filter.forPlayers("#A").trophies(100000, 110000).build()))
                .extracting(Battle::getBattleTime).containsExactlyInAnyOrder(T2, T3);
        assertThat(service.findBattles(Filter.forPlayers("#A").trophies(null, 99999).build()))
                .extracting(Battle::getBattleTime).containsExactly(T1);
        assertThat(service.findBattles(Filter.forPlayers("#A").trophies(110000, null).build()))
                .extracting(Battle::getBattleTime).containsExactly(T3);
    }

    @Test
    void filtersByRankAndGameMode() {
        Battle legendary = battle(T1, "#A");
        Battle masters = battle(T2, "#A");
        masters.setRank("MASTERS I");
        masters.setGameMode("gemGrab");
        Battle mastersHeist = battle(T3, "#A");
        mastersHeist.setRank("MASTERS I");
        battleRepository.saveAll(List.of(legendary, masters, mastersHeist));

        assertThat(service.findBattles(Filter.forPlayers("#A").ranks("MASTERS I").build()))
                .extracting(Battle::getBattleTime).containsExactlyInAnyOrder(T2, T3);
        assertThat(service.findBattles(Filter.forPlayers("#A").ranks("MASTERS I").modes("gemGrab").build()))
                .extracting(Battle::getBattleTime).containsExactly(T2);
    }

    @Test
    void mapOverridesGameMode() {
        Battle heist = battle(T1, "#A");
        Battle gemGrab = battle(T2, "#A");
        gemGrab.setGameMode("gemGrab");
        gemGrab.setMap("Hard Rock Mine");
        battleRepository.saveAll(List.of(heist, gemGrab));

        // The game mode says heist, but the map is from the gem grab battle: the map wins
        List<Battle> result = service.findBattles(
                Filter.forPlayers("#A").modes("heist").maps("Hard Rock Mine").build());

        assertThat(result).extracting(Battle::getBattleTime).containsExactly(T2);
    }

    @Test
    void limitTakesTheMostRecentBattlesAfterTheOtherFilters() {
        Battle old = battle(T1, "#A");
        Battle middle = battle(T2, "#A");
        Battle recent = battle(T3, "#A");
        Battle newestButRanked = battle(T4, "#A");
        newestButRanked.setBattleType(BattleType.ranked);
        battleRepository.saveAll(List.of(old, middle, recent, newestButRanked));

        List<Battle> result = service.findBattles(
                Filter.forPlayers("#A").types(BattleType.soloRanked).limit(2).build());

        // The newest battle overall is ranked, so it is not part of the filtered battles
        assertThat(result).extracting(Battle::getBattleTime).containsExactly(T3, T2);
    }

    @Test
    void sharedMatchOfTwoSelectedPlayersIsCountedOnce() {
        // The match is stored once, under #A, and #B is Ally 2
        Battle shared = battle(T1, "#A");
        shared.setAlly2("#B");
        battleRepository.save(shared);

        List<Battle> result = service.findBattles(Filter.forPlayers("#A", "#B").build());

        assertThat(result).hasSize(1);
    }

    @Test
    void throwsWhenAPlayerIsNotRegistered() {
        assertThatThrownBy(() -> service.findBattles(Filter.forPlayers("#A", "#UNKNOWN").build()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("#UNKNOWN");
    }

    // ---------- Filter options ----------

    @Test
    void filterOptionsListTheExistingValuesOfThePlayers() {
        Battle first = battle(T1, "#A");
        Battle second = battle(T3, "#A");
        second.setBattleType(BattleType.ranked);
        second.setGameMode("gemGrab");
        second.setMap("Hard Rock Mine");
        second.setRank("MASTERS I");
        second.setTrophies(120000);
        // Another player: its values must not appear
        Battle other = battle(T2, "#C");
        other.setMap("Other Map");
        battleRepository.saveAll(List.of(first, second, other));

        FilterOptionsResponse options = service.getFilterOptions(List.of("a"));

        assertThat(options.battleTypes()).containsExactly(BattleType.ranked, BattleType.soloRanked);
        assertThat(options.ranks()).containsExactly("LEGENDARY I", "MASTERS I");
        assertThat(options.gameModes()).containsExactly("gemGrab", "heist");
        assertThat(options.maps()).containsExactly("Hard Rock Mine", "Kaboom Canyon");
        assertThat(options.trophiesMin()).isEqualTo(100000);
        assertThat(options.trophiesMax()).isEqualTo(120000);
        assertThat(options.firstBattle()).isEqualTo("2026-10-01T10:00:00");
        assertThat(options.lastBattle()).isEqualTo("2026-10-01T12:00:00");
    }

    @Test
    void filterOptionsAreEmptyWhenThePlayerHasNoBattles() {
        FilterOptionsResponse options = service.getFilterOptions(List.of("#B"));

        assertThat(options.battleTypes()).isEmpty();
        assertThat(options.ranks()).isEmpty();
        assertThat(options.gameModes()).isEmpty();
        assertThat(options.maps()).isEmpty();
        assertThat(options.trophiesMin()).isNull();
        assertThat(options.trophiesMax()).isNull();
        assertThat(options.firstBattle()).isNull();
        assertThat(options.lastBattle()).isNull();
    }

    // ---------- Helpers ----------

    private void registerPlayers(String... tags) {
        for (String tag : tags) {
            Player player = new Player();
            player.setTag(tag);
            player.setName("name" + tag);
            playerRepository.save(player);
        }
    }

    // A normal battle of the player. Tests change what they need before saving it
    private static Battle battle(String time, String owner) {
        Battle battle = new Battle();
        battle.setId(Battle.buildId(time, owner));
        battle.setBattleTime(time);
        battle.setBattleType(BattleType.soloRanked);
        battle.setGameMode("heist");
        battle.setMap("Kaboom Canyon");
        battle.setRank("LEGENDARY I");
        battle.setTrophies(100000);
        battle.setBattleResult("victory");
        battle.setAlly1(owner);
        return battle;
    }

    // Small builder, so each test only writes the filters it needs
    private static class Filter {
        private final List<String> players;
        private List<BattleType> types = List.of();
        private List<String> ranks = List.of();
        private Integer trophiesMin;
        private Integer trophiesMax;
        private List<String> modes = List.of();
        private List<String> maps = List.of();
        private String from;
        private String to;
        private Integer limit;

        private Filter(String... players) {
            this.players = List.of(players);
        }

        static Filter forPlayers(String... players) {
            return new Filter(players);
        }

        Filter types(BattleType... values) {
            this.types = List.of(values);
            return this;
        }

        Filter ranks(String... values) {
            this.ranks = List.of(values);
            return this;
        }

        Filter trophies(Integer min, Integer max) {
            this.trophiesMin = min;
            this.trophiesMax = max;
            return this;
        }

        Filter modes(String... values) {
            this.modes = List.of(values);
            return this;
        }

        Filter maps(String... values) {
            this.maps = List.of(values);
            return this;
        }

        Filter time(String from, String to) {
            this.from = from;
            this.to = to;
            return this;
        }

        Filter limit(int value) {
            this.limit = value;
            return this;
        }

        BattleFilter build() {
            return new BattleFilter(players, types, ranks, trophiesMin, trophiesMax, modes, maps, from, to, limit);
        }
    }
}