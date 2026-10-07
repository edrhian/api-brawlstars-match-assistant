package com.edrhian.brawlmatchstats.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for the "players" table. The ID is the player tag (a String).
 */
public interface PlayerRepository extends JpaRepository<Player, String> {

    // All players whose battles we want to store (used later by the scheduled job)
    List<Player> findByTrackingEnabledTrue();
}