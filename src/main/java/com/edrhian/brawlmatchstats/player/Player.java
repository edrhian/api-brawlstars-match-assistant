package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A BrawlStars player. This class is the "PLAYERS" table.
 */
@Entity
@Table(name = "players")
public class Player {

    // The BrawlStars tag, example: #2VU8PGCC
    @Id
    private String tag;

    // The player's name (it can change over time)
    private String name;

    // Current trophy count
    private Integer trophies;

    // Current ranked rank, saved as the API sends it, example: LEGENDARY I
    private String rankedRank;

    // Do we want to store the battles of this player?
    @Column(nullable = false)
    private boolean trackingEnabled = false;

    // Only battles of these types are saved, example: ranked,soloRanked
    @Convert(converter = TrackingTypesConverter.class)
    private List<BattleType> trackingTypes = new ArrayList<>();

    // Last time the profile was updated, example: 2026-10-07T11:00:39
    private String lastUpdated;

    public Player() {
        // JPA needs an empty constructor
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTrophies() {
        return trophies;
    }

    public void setTrophies(Integer trophies) {
        this.trophies = trophies;
    }

    public String getRankedRank() {
        return rankedRank;
    }

    public void setRankedRank(String rankedRank) {
        this.rankedRank = rankedRank;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }

    public List<BattleType> getTrackingTypes() {
        return trackingTypes;
    }

    public void setTrackingTypes(List<BattleType> trackingTypes) {
        this.trackingTypes = trackingTypes;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}