package com.edrhian.brawlmatchstats.analysis;

import java.util.List;

/**
 * Response of GET /analysis/brawlers.
 *
 * - battles: how many battles passed the filters (before "with" and "against")
 * - brawlers: the results of each Brawler, most played first
 */
public record BrawlerAnalysisResponse(int battles, List<BrawlerStatsDto> brawlers) {
}