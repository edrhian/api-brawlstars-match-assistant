package com.edrhian.brawlmatchstats.analysis;

import java.util.List;

/**
 * Response of GET /analysis/brawlers.
 *
 * - battles: how many battles passed the filters (before "with", "against" and "minGames")
 * - sort: the order that was applied, like ["games:desc", "brawler:asc"]
 * - brawlers: the results of each Brawler, in that order
 */
public record BrawlerAnalysisResponse(int battles, List<String> sort, List<BrawlerStatsDto> brawlers) {
}