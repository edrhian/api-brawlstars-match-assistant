package com.edrhian.brawlmatchstats.player;

/**
 * Thrown when we try to add a player that is already in the database.
 */
public class PlayerAlreadyExistsException extends RuntimeException {

    public PlayerAlreadyExistsException(String tag) {
        super("Player already registered: " + tag);
    }
}