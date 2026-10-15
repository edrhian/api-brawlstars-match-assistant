package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.shared.BattleType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisRequestTest {

    @Test
    void normalizesAndDeduplicatesPlayerTags() {
        BattleFilter filter = request(List.of("2vu8pgcc", "#2VU8PGCC", " p0pulrc ")).toFilter();

        assertThat(filter.players()).containsExactly("#2VU8PGCC", "#P0PULRC");
    }

    @Test
    void missingOptionalFiltersBecomeEmpty() {
        BattleFilter filter = new AnalysisRequest(List.of("#A"), false, null, Arrays.asList("LEGENDARY I", " ", null),
                null, null, null, List.of(""), null, null, null, null).toFilter();

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
        BattleFilter filter = new AnalysisRequest(List.of("#A"), false, List.of(BattleType.ranked, BattleType.ranked),
                null, 100000, 120000, List.of("heist"), List.of("Kaboom Canyon"), null, null, 50, null).toFilter();

        assertThat(filter.battleTypes()).containsExactly(BattleType.ranked);
        assertThat(filter.trophiesMin()).isEqualTo(100000);
        assertThat(filter.trophiesMax()).isEqualTo(120000);
        assertThat(filter.gameModes()).containsExactly("heist");
        assertThat(filter.maps()).containsExactly("Kaboom Canyon");
        assertThat(filter.limit()).isEqualTo(50);
    }

    @Test
    void convertsDatesToTheStoredFormat() {
        BattleFilter filter = new AnalysisRequest(List.of("#A"), false, null, null, null, null, null, null,
                "2026-10-01", "2026-10-02", null, null).toFilter();

        // "from" is the start of the day and "to" is the end of the day
        assertThat(filter.fromTime()).isEqualTo("20261001T000000.000Z");
        assertThat(filter.toTime()).isEqualTo("20261002T235959.999Z");
    }

    @Test
    void acceptsDateAndTime() {
        BattleFilter filter = new AnalysisRequest(List.of("#A"), false, null, null, null, null, null, null,
                "2026-10-01T10:30:00", null, null, null).toFilter();

        assertThat(filter.fromTime()).isEqualTo("20261001T103000.000Z");
    }

//    Omitted after adding allPlayers filter
//    @Test
//    void requiresAtLeastOnePlayer() {
//        assertThatThrownBy(() -> request(null).toFilter()).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> request(List.of()).toFilter()).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> request(List.of(" ")).toFilter()).isInstanceOf(IllegalArgumentException.class);
//    }

    @Test
    void rejectsInvalidDate() {
        AnalysisRequest request = new AnalysisRequest(List.of("#A"), false,  null, null, null, null, null, null,
                "01/10/2026", null, null, null);

        assertThatThrownBy(request::toFilter)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date");
    }

    @Test
    void rejectsFromAfterTo() {
        AnalysisRequest request = new AnalysisRequest(List.of("#A"), false, null, null, null, null, null, null,
                "2026-10-05", "2026-10-01", null, null);

        assertThatThrownBy(request::toFilter).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTrophiesMinGreaterThanMax() {
        AnalysisRequest request = new AnalysisRequest(List.of("#A"), false, null, null, 120000, 100000, null, null,
                null, null, null, null);

        assertThatThrownBy(request::toFilter).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLimitBelowOne() {
        AnalysisRequest request = new AnalysisRequest(List.of("#A"), false, null, null, null, null, null, null,
                null, null, 0, null);

        assertThatThrownBy(request::toFilter).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void minGamesDefaultsToOneAndRejectsLowerValues() {
        assertThat(request(List.of("#A")).minGamesOrDefault()).isEqualTo(1);

        AnalysisRequest invalid = new AnalysisRequest(List.of("#A"), false, null, null, null, null, null, null,
                null, null, null, 0);
        assertThatThrownBy(invalid::minGamesOrDefault).isInstanceOf(IllegalArgumentException.class);
    }

    private static AnalysisRequest request(List<String> players) {
        return new AnalysisRequest(players, false, null, null, null, null, null, null, null, null, null, null);
    }
}