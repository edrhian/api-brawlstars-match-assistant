package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Optional body of POST /players/{tag}. Anything left out uses the default:
 * tracking enabled and all the battle types.
 */
public record RegisterPlayerRequest(
        Boolean trackingEnabled,
        @Size(min = 1, message = "trackingTypes cannot be empty") List<BattleType> trackingTypes
) {
}