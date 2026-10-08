package com.edrhian.brawlmatchstats.shared;

/**
 * The battle types we store. The names are written exactly like the BrawlStars API
 * sends them, so BattleType.valueOf("soloRanked") works directly.
 *
 * Careful, the API names are misleading:
 * - ranked:     trophy games (BrawlStars calls them "ranked" for some reason)
 * - soloRanked: the competitive mode with ranks, even if it was played in a premade party
 * - friendly:   custom friendly battles, included because they are used in scrims
 *
 * Any other type sent by the API (trophyRoad, challenge...) is not stored.
 */
public enum BattleType {
    ranked,
    soloRanked,
    friendly
}