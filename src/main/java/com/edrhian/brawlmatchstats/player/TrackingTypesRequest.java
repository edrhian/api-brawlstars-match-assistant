package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Body of PUT /players/{tag}/tracking/types.
 */
public record TrackingTypesRequest(
        @NotEmpty(message = "trackingTypes cannot be empty") List<BattleType> trackingTypes
) {
}