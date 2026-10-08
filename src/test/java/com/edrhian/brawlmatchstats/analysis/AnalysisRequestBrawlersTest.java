package com.edrhian.brawlmatchstats.analysis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisRequestBrawlersTest {

    @Test
    void writesBrawlersInUppercaseWithoutRepeatedOrEmptyOnes() {
        List<String> brawlers = AnalysisRequest.cleanBrawlers(List.of(" shelly ", "COLT", "Shelly", " "));

        assertThat(brawlers).containsExactly("SHELLY", "COLT");
    }

    @Test
    void requiresAtLeastOneBrawler() {
        assertThatThrownBy(() -> AnalysisRequest.cleanBrawlers(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AnalysisRequest.cleanBrawlers(List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AnalysisRequest.cleanBrawlers(List.of(" "))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsAtMostThreeDifferentBrawlers() {
        assertThat(AnalysisRequest.cleanBrawlers(List.of("A", "B", "C"))).hasSize(3);
        assertThatThrownBy(() -> AnalysisRequest.cleanBrawlers(List.of("A", "B", "C", "D")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 3");
    }
}