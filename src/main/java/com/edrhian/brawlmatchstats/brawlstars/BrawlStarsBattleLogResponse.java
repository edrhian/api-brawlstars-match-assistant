package com.edrhian.brawlmatchstats.brawlstars;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * The part of GET /players/{tag}/battlelog that we need. Other fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrawlStarsBattleLogResponse(List<Item> items) {

    // One battle of the list
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String battleTime, Event event, BattleDetails battle) {
    }

    // Example: mode = heist, map = Kaboom Canyon
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Event(String mode, String map) {
    }

    // type = soloRanked, result = victory. "teams" can be null in modes without teams (like Showdown)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BattleDetails(String mode, String type, String result, List<List<Participant>> teams) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Participant(String tag, String name, Brawler brawler) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Brawler(String name) {
    }
}