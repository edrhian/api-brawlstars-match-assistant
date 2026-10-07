package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.shared.BattleType;

/**
 * The battle data we send and receive through the API.
 * It has the same fields as the Battle entity, but it is not tied to the database.
 */
public record BattleDto(
        String id,
        String battleTime,
        BattleType battleType,
        String rank,
        Integer trophies,
        String gameMode,
        String map,
        String ally1,
        String ally1Brawler,
        String ally2,
        String ally2Brawler,
        String ally3,
        String ally3Brawler,
        String enemy1,
        String enemy1Brawler,
        String enemy2,
        String enemy2Brawler,
        String enemy3,
        String enemy3Brawler,
        String battleResult,
        String fetchedAt
) {
}