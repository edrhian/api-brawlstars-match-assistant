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

    // Values that exist in the database, to fill the filter selectors
    @GetMapping("/filter-options")
    public FilterOptionsResponse filterOptions(@RequestParam List<String> players) {
        return analysisService.getFilterOptions(players);
    }
}