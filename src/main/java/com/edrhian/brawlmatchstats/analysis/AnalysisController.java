package com.edrhian.brawlmatchstats.analysis;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Battle analysis endpoints. The filters are query parameters (see AnalysisRequest),
 */
@RestController
@RequestMapping("/analysis")
@CrossOrigin(origins = "*")
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
    public FilterOptionsResponse filterOptions(AnalysisRequest request) {
        // Extract players and the allPlayers flag from the request
        BattleFilter filter = request.toFilter();
        return analysisService.getFilterOptions(filter.players(), filter.allPlayers());
    }
}