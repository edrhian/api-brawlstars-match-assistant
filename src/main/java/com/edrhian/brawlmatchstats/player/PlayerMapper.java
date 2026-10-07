package com.edrhian.brawlmatchstats.player;

import java.util.ArrayList;

/**
 * Converts between the Player entity and PlayerDto.
 */
public final class PlayerMapper {

    private PlayerMapper() {
        // Only static methods, nobody needs to create this class
    }

    public static PlayerDto toDto(Player player) {
        return new PlayerDto(
                player.getTag(),
                player.getName(),
                player.getTrophies(),
                player.getRankedRank(),
                player.isTrackingEnabled(),
                player.getTrackingTypes(),
                player.getLastUpdated()
        );
    }

    public static Player toEntity(PlayerDto dto) {
        Player player = new Player();
        player.setTag(dto.tag());
        player.setName(dto.name());
        player.setTrophies(dto.trophies());
        player.setRankedRank(dto.rankedRank());
        player.setTrackingEnabled(dto.trackingEnabled());
        player.setTrackingTypes(dto.trackingTypes() == null ? new ArrayList<>() : dto.trackingTypes());
        player.setLastUpdated(dto.lastUpdated());
        return player;
    }
}