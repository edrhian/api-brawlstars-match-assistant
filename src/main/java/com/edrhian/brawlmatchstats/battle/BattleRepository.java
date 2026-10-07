package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.shared.BattleType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for the "battles" table. The ID is battleTime + "_" + ally1.
 */
public interface BattleRepository extends JpaRepository<Battle, String> {

    // Used by the duplicate check: is this battle already stored for this player?
    boolean existsByAlly1AndBattleTimeAndBattleType(String ally1, String battleTime, BattleType battleType);
}