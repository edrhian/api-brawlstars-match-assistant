package com.edrhian.brawlmatchstats.analysis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Battle analysis endpoints. The filters are query parameters (see AnalysisRequest).
 * Players are required: players=2VU8PGCC (tags without "#") or allPlayers=true.
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Winrate of every Brawler, on any team.
     * with=SHELLY: only when Shelly is a teammate. against=BROCK: only when Brock is in the other team.
     */
    @GetMapping("/brawlers")
    public BrawlerAnalysisResponse brawlers(AnalysisRequest request) {
        return analysisService.analyzeBrawlers(
                request.toFilter(), request.toInput(), request.minGamesOrDefault());
    }

    // Values that exist in the database, to fill the filter selectors
    @GetMapping("/filter-options")
    public FilterOptionsResponse filterOptions(@RequestParam(required = false) List<String> players,
                                               @RequestParam(required = false) Boolean allPlayers) {
        boolean everyone = Boolean.TRUE.equals(allPlayers);
        List<String> tags = AnalysisRequest.resolvePlayers(players, everyone);
        return analysisService.getFilterOptions(tags, everyone);
    }
}