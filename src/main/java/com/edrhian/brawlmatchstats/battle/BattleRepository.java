package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.shared.BattleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Database access for the "battles" table. The ID is battleTime + "_" + ally1.
 *
 * JpaSpecificationExecutor lets the analysis module build the filters dynamically.
 */
public interface BattleRepository extends JpaRepository<Battle, String>, JpaSpecificationExecutor<Battle> {

    // A battle belongs to a player if the player is in any of the 3 ally slots
    String PLAYER_IN_TEAM = "(b.ally1 in :tags or b.ally2 in :tags or b.ally3 in :tags)";

    // Used by the duplicate check: is this battle already stored for this player?
    boolean existsByAlly1AndBattleTimeAndBattleType(String ally1, String battleTime, BattleType battleType);

    // ---------- Existing values, used to build the filter options ----------

    @Query("select distinct b.battleType from Battle b where " + PLAYER_IN_TEAM
            + " and b.battleType is not null order by b.battleType")
    List<BattleType> findDistinctBattleTypes(@Param("tags") Collection<String> tags);

    @Query("select distinct b.rank from Battle b where " + PLAYER_IN_TEAM
            + " and b.rank is not null order by b.rank")
    List<String> findDistinctRanks(@Param("tags") Collection<String> tags);

    @Query("select distinct b.gameMode from Battle b where " + PLAYER_IN_TEAM
            + " and b.gameMode is not null order by b.gameMode")
    List<String> findDistinctGameModes(@Param("tags") Collection<String> tags);

    @Query("select distinct b.map from Battle b where " + PLAYER_IN_TEAM
            + " and b.map is not null order by b.map")
    List<String> findDistinctMaps(@Param("tags") Collection<String> tags);

    @Query("select new com.edrhian.brawlmatchstats.battle.BattleRange("
            + "min(b.trophies), max(b.trophies), min(b.battleTime), max(b.battleTime)) "
            + "from Battle b where " + PLAYER_IN_TEAM)
    BattleRange findRangeByPlayers(@Param("tags") Collection<String> tags);
}