package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.shared.BattleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Database access for the "battles" table. The ID is battleTime + "_" + the smallest tag of the 6 players.
 *
 * JpaSpecificationExecutor lets the analysis module build the filters dynamically.
 */
public interface BattleRepository extends JpaRepository<Battle, String>, JpaSpecificationExecutor<Battle> {

    // A battle belongs to a player if the player is in any of the 6 slots (3 allies and 3 enemies)
    String PLAYER_IN_BATTLE = "(b.ally1 in :tags or b.ally2 in :tags or b.ally3 in :tags"
            + " or b.enemy1 in :tags or b.enemy2 in :tags or b.enemy3 in :tags)";

    // ---------- Existing values, used to build the filter options ----------

    @Query("select distinct b.battleType from Battle b where " + PLAYER_IN_BATTLE
            + " and b.battleType is not null order by b.battleType")
    List<BattleType> findDistinctBattleTypes(@Param("tags") Collection<String> tags);

    @Query("select distinct b.rank from Battle b where " + PLAYER_IN_BATTLE
            + " and b.rank is not null order by b.rank")
    List<String> findDistinctRanks(@Param("tags") Collection<String> tags);

    @Query("select distinct b.gameMode from Battle b where " + PLAYER_IN_BATTLE
            + " and b.gameMode is not null order by b.gameMode")
    List<String> findDistinctGameModes(@Param("tags") Collection<String> tags);

    @Query("select distinct b.map from Battle b where " + PLAYER_IN_BATTLE
            + " and b.map is not null order by b.map")
    List<String> findDistinctMaps(@Param("tags") Collection<String> tags);

    @Query("select new com.edrhian.brawlmatchstats.battle.BattleRange("
            + "min(b.trophies), max(b.trophies), min(b.battleTime), max(b.battleTime)) "
            + "from Battle b where " + PLAYER_IN_BATTLE)
    BattleRange findRangeByPlayers(@Param("tags") Collection<String> tags);
}