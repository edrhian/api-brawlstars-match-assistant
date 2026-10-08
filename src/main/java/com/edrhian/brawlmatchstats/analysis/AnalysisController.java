package com.edrhian.brawlmatchstats.analysis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Battle analysis endpoints. The filters are query parameters (see AnalysisRequest),
 * and players (tags without "#") is the only required one.
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // Pick comfort
    @GetMapping("/ally-brawlers")
    public BrawlerAnalysisResponse allyBrawlers(AnalysisRequest request) {
        return analysisService.analyzeAllyBrawlers(request.toFilter(), request.minGamesOrDefault());
    }

    // Confrontation comfort
    @GetMapping("/enemy-brawlers")
    public BrawlerAnalysisResponse enemyBrawlers(AnalysisRequest request) {
        return analysisService.analyzeEnemyBrawlers(request.toFilter(), request.minGamesOrDefault());
    }

    // Which Brawlers work well together with the given ones (brawlers=SHELLY&brawlers=COLT)
    @GetMapping("/ally-synergy")
    public InputAnalysisResponse allySynergy(AnalysisRequest request, @RequestParam List<String> brawlers) {
        return analysisService.analyzeAllySynergy(
                request.toFilter(), AnalysisRequest.cleanBrawlers(brawlers), request.minGamesOrDefault());
    }

    // Which of our picks work well against the given enemy Brawlers (brawlers=BROCK&brawlers=CARL)
    @GetMapping("/enemy-counters")
    public InputAnalysisResponse enemyCounters(AnalysisRequest request, @RequestParam List<String> brawlers) {
        return analysisService.analyzeEnemyCounters(
                request.toFilter(), AnalysisRequest.cleanBrawlers(brawlers), request.minGamesOrDefault());
    }

    // Values that exist in the database, to fill the filter selectors
    @GetMapping("/filter-options")
    public FilterOptionsResponse filterOptions(@RequestParam List<String> players) {
        return analysisService.getFilterOptions(players);
    }
}