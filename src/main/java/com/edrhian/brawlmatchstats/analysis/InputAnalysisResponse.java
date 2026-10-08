package com.edrhian.brawlmatchstats.analysis;

import java.util.List;

/**
 * Response of the synergy and counter endpoints.
 *
 * - inputBrawlers: the Brawlers that were asked for (uppercase)
 * - overall: result of all the filtered battles
 * - withInput: result of the battles that contain the input Brawlers
 * - brawlers: for synergy, the results of each partner together with the input Brawlers;
 *   for counters, the results of each ally pick against the input Brawlers
 */
public record InputAnalysisResponse(
        List<String> inputBrawlers,
        StatsDto overall,
        StatsDto withInput,
        List<BrawlerStatsDto> brawlers
) {
}