package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisRequestTest {

    // ---------- Players ----------

    @Test
    void normalizesAndDeduplicatesPlayerTags() {
        BattleFilter filter = Request.players("2vu8pgcc", "#2VU8PGCC", " p0pulrc ").build().toFilter();

        assertThat(filter.players()).containsExactly("#2VU8PGCC", "#P0PULRC");
        assertThat(filter.allPlayers()).isFalse();
    }

    @Test
    void requiresPlayersOrAllPlayers() {
        assertThatThrownBy(() -> new Request().build().toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allPlayers");
        assertThatThrownBy(() -> Request.players(" ").build().toFilter())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Request().allPlayers(false).build().toFilter())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allPlayersNeedsNoTags() {
        BattleFilter filter = new Request().allPlayers(true).build().toFilter();

        assertThat(filter.allPlayers()).isTrue();
        assertThat(filter.players()).isEmpty();
    }

    @Test
    void rejectsPlayersTogetherWithAllPlayers() {
        assertThatThrownBy(() -> Request.players("#A").allPlayers(true).build().toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not both");
    }

    // ---------- Filters ----------

    @Test
    void missingOptionalFiltersBecomeEmpty() {
        BattleFilter filter = Request.players("#A").ranks("LEGENDARY I", " ", null).maps("").build().toFilter();

        assertThat(filter.battleTypes()).isEmpty();
        assertThat(filter.ranks()).containsExactly("LEGENDARY I");
        assertThat(filter.gameModes()).isEmpty();
        assertThat(filter.maps()).isEmpty();
        assertThat(filter.fromTime()).isNull();
        assertThat(filter.toTime()).isNull();
        assertThat(filter.limit()).isNull();
    }

    @Test
    void keepsTheValuesThatWereGiven() {
        BattleFilter filter = Request.players("#A")
                .types(BattleType.ranked, BattleType.ranked)
                .trophies(100000, 120000)
                .modes("heist").maps("Kaboom Canyon").limit(50)
                .build().toFilter();

        assertThat(filter.battleTypes()).containsExactly(BattleType.ranked);
        assertThat(filter.trophiesMin()).isEqualTo(100000);
        assertThat(filter.trophiesMax()).isEqualTo(120000);
        assertThat(filter.gameModes()).containsExactly("heist");
        assertThat(filter.maps()).containsExactly("Kaboom Canyon");
        assertThat(filter.limit()).isEqualTo(50);
    }

    @Test
    void convertsDatesToTheStoredFormat() {
        BattleFilter filter = Request.players("#A").time("2026-10-01", "2026-10-02").build().toFilter();

        // "from" is the start of the day and "to" is the end of the day
        assertThat(filter.fromTime()).isEqualTo("20261001T000000.000Z");
        assertThat(filter.toTime()).isEqualTo("20261002T235959.999Z");
    }

    @Test
    void acceptsDateAndTime() {
        BattleFilter filter = Request.players("#A").time("2026-10-01T10:30:00", null).build().toFilter();

        assertThat(filter.fromTime()).isEqualTo("20261001T103000.000Z");
    }

    @Test
    void rejectsInvalidDate() {
        assertThatThrownBy(() -> Request.players("#A").time("01/10/2026", null).build().toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date");
    }

    @Test
    void rejectsFromAfterTo() {
        assertThatThrownBy(() -> Request.players("#A").time("2026-10-05", "2026-10-01").build().toFilter())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTrophiesMinGreaterThanMax() {
        assertThatThrownBy(() -> Request.players("#A").trophies(120000, 100000).build().toFilter())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLimitBelowOne() {
        assertThatThrownBy(() -> Request.players("#A").limit(0).build().toFilter())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void minGamesDefaultsToOneAndRejectsLowerValues() {
        assertThat(Request.players("#A").build().minGamesOrDefault()).isEqualTo(1);
        assertThatThrownBy(() -> Request.players("#A").minGames(0).build().minGamesOrDefault())
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- with / against ----------

    @Test
    void brawlersAreUppercasedAndDeduplicated() {
        BrawlerInput input = Request.players("#A")
                .with("shelly", " Colt", "SHELLY")
                .against("brock")
                .build().toInput();

        assertThat(input.with()).containsExactly("SHELLY", "COLT");
        assertThat(input.against()).containsExactly("BROCK");
    }

    @Test
    void brawlersAreOptional() {
        BrawlerInput input = Request.players("#A").build().toInput();

        assertThat(input.with()).isEmpty();
        assertThat(input.against()).isEmpty();
    }

    @Test
    void rejectsMoreThanTwoWithBrawlers() {
        assertThatThrownBy(() -> Request.players("#A").with("SHELLY", "COLT", "SPIKE").build().toInput())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("with");
    }

    @Test
    void rejectsMoreThanThreeAgainstBrawlers() {
        assertThatThrownBy(() -> Request.players("#A").against("A", "B", "C", "D").build().toInput())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("against");
    }

    // ---------- Small builder, so each test only writes what it needs ----------

    private static class Request {
        private List<String> players;
        private Boolean allPlayers;
        private List<BattleType> types;
        private List<String> ranks;
        private Integer trophiesMin;
        private Integer trophiesMax;
        private List<String> modes;
        private List<String> maps;
        private String from;
        private String to;
        private Integer limit;
        private Integer minGames;
        private List<String> with;
        private List<String> against;

        static Request players(String... tags) {
            Request request = new Request();
            request.players = Arrays.asList(tags);
            return request;
        }

        Request allPlayers(boolean value) {
            this.allPlayers = value;
            return this;
        }

        Request types(BattleType... values) {
            this.types = Arrays.asList(values);
            return this;
        }

        Request ranks(String... values) {
            this.ranks = Arrays.asList(values);
            return this;
        }

        Request trophies(Integer min, Integer max) {
            this.trophiesMin = min;
            this.trophiesMax = max;
            return this;
        }

        Request modes(String... values) {
            this.modes = Arrays.asList(values);
            return this;
        }

        Request maps(String... values) {
            this.maps = Arrays.asList(values);
            return this;
        }

        Request time(String from, String to) {
            this.from = from;
            this.to = to;
            return this;
        }

        Request limit(int value) {
            this.limit = value;
            return this;
        }

        Request minGames(int value) {
            this.minGames = value;
            return this;
        }

        Request with(String... values) {
            this.with = Arrays.asList(values);
            return this;
        }

        Request against(String... values) {
            this.against = Arrays.asList(values);
            return this;
        }

        AnalysisRequest build() {
            return new AnalysisRequest(players, allPlayers, types, ranks, trophiesMin, trophiesMax,
                    modes, maps, from, to, limit, minGames, with, against);
        }
    }
}