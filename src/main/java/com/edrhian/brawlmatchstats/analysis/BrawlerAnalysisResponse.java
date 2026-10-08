package com.edrhian.brawlmatchstats.analysis;

import java.util.List;

/**
 * Response of the ally and enemy Brawler endpoints.
 * "overall" is the result of all the filtered battles, to compare each Brawler against.
 */
public record BrawlerAnalysisResponse(StatsDto overall, List<BrawlerStatsDto> brawlers) {
}