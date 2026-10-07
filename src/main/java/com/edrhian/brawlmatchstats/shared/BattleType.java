package com.edrhian.brawlmatchstats.shared;

/**
 * The battle types we want to store.
 *
 * The names are written exactly like the BrawlStars API sends them
 * (for example "soloRanked"), so BattleType.valueOf("soloRanked") works directly.
 */
public enum BattleType {
    ranked,
    soloRanked,
    friendly
}