package com.edrhian.brawlmatchstats.analysis;

import java.util.List;

/**
 * Optional brawlers that narrow down the analysis (both lists can be empty):
 *
 * - with: brawlers that must be in the same team as the brawler we are counting (max 2)
 * - against: brawlers that must be in the opposite team (max 3)
 *
 * Names are uppercase, like the API sends them.
 */
public record BrawlerInput(List<String> with, List<String> against) {

    public static final BrawlerInput NONE = new BrawlerInput(List.of(), List.of());
}