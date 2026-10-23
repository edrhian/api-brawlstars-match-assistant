package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

/**
 * One battle seen from the point of view of Ally 1 (the tracked player).
 * This class is the "BATTLES" table.
 */
@Entity
@Table(name = "battles", indexes = {
        @Index(name = "idx_battles_ally1_time", columnList = "ally1, battleTime"),
        @Index(name = "idx_battles_ally2_time", columnList = "ally2, battleTime"),
        @Index(name = "idx_battles_ally3_time", columnList = "ally3, battleTime"),
        @Index(name = "idx_battles_enemy1_time", columnList = "enemy1, battleTime"),
        @Index(name = "idx_battles_enemy2_time", columnList = "enemy2, battleTime"),
        @Index(name = "idx_battles_enemy3_time", columnList = "enemy3, battleTime")
})
public class Battle {

    // BrawlStars battles have no ID, so we build one: battleTime + "_" + the smallest tag of the 6 players
    // Example: 20261007T070930.000Z_#2R0P8LPR9
    // The same battle read from any of its players gets the same ID.
    @Id
    private String id;

    // Exactly as the API sends it, example: 20261007T070930.000Z
    private String battleTime;

    @Enumerated(EnumType.STRING)
    private BattleType battleType;

    // Ranked rank of the tracked player when the battle was fetched
    private String rank;

    // Trophies of the tracked player when the battle was fetched
    private Integer trophies;

    // Example: gemGrab
    private String gameMode;

    // Example: Double Swoosh
    private String map;

    // Ally 1 is the tracked player (the one whose battles we store)
    private String ally1;
    private String ally1Brawler;

    private String ally2;
    private String ally2Brawler;

    private String ally3;
    private String ally3Brawler;

    private String enemy1;
    private String enemy1Brawler;

    private String enemy2;
    private String enemy2Brawler;

    private String enemy3;
    private String enemy3Brawler;

    // Result from Ally 1's point of view, example: victory, defeat
    private String battleResult;

    // When we stored the battle, example: 2026-10-07T11:00:39
    private String fetchedAt;

    public Battle() {
        // JPA needs an empty constructor
    }

    /**
     * Builds the battle ID from the battle time and the tags of the players in the battle.
     * It uses the alphabetically smallest tag, so every player that sees this battle builds the same ID.
     * One player cannot finish two battles at the same second, so it is unique.
     */
    public static String buildId(String battleTime, String... tags) {
        String smallest = null;
        for (String tag : tags) {
            if (tag != null && (smallest == null || tag.compareTo(smallest) < 0)) {
                smallest = tag;
            }
        }
        return battleTime + "_" + smallest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(String battleTime) {
        this.battleTime = battleTime;
    }

    public BattleType getBattleType() {
        return battleType;
    }

    public void setBattleType(BattleType battleType) {
        this.battleType = battleType;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getTrophies() {
        return trophies;
    }

    public void setTrophies(Integer trophies) {
        this.trophies = trophies;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getAlly1() {
        return ally1;
    }

    public void setAlly1(String ally1) {
        this.ally1 = ally1;
    }

    public String getAlly1Brawler() {
        return ally1Brawler;
    }

    public void setAlly1Brawler(String ally1Brawler) {
        this.ally1Brawler = ally1Brawler;
    }

    public String getAlly2() {
        return ally2;
    }

    public void setAlly2(String ally2) {
        this.ally2 = ally2;
    }

    public String getAlly2Brawler() {
        return ally2Brawler;
    }

    public void setAlly2Brawler(String ally2Brawler) {
        this.ally2Brawler = ally2Brawler;
    }

    public String getAlly3() {
        return ally3;
    }

    public void setAlly3(String ally3) {
        this.ally3 = ally3;
    }

    public String getAlly3Brawler() {
        return ally3Brawler;
    }

    public void setAlly3Brawler(String ally3Brawler) {
        this.ally3Brawler = ally3Brawler;
    }

    public String getEnemy1() {
        return enemy1;
    }

    public void setEnemy1(String enemy1) {
        this.enemy1 = enemy1;
    }

    public String getEnemy1Brawler() {
        return enemy1Brawler;
    }

    public void setEnemy1Brawler(String enemy1Brawler) {
        this.enemy1Brawler = enemy1Brawler;
    }

    public String getEnemy2() {
        return enemy2;
    }

    public void setEnemy2(String enemy2) {
        this.enemy2 = enemy2;
    }

    public String getEnemy2Brawler() {
        return enemy2Brawler;
    }

    public void setEnemy2Brawler(String enemy2Brawler) {
        this.enemy2Brawler = enemy2Brawler;
    }

    public String getEnemy3() {
        return enemy3;
    }

    public void setEnemy3(String enemy3) {
        this.enemy3 = enemy3;
    }

    public String getEnemy3Brawler() {
        return enemy3Brawler;
    }

    public void setEnemy3Brawler(String enemy3Brawler) {
        this.enemy3Brawler = enemy3Brawler;
    }

    public String getBattleResult() {
        return battleResult;
    }

    public void setBattleResult(String battleResult) {
        this.battleResult = battleResult;
    }

    public String getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(String fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}