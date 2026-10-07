package com.edrhian.brawlmatchstats.brawlstars;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The part of GET /players/{tag} that we need. Other fields of the response are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrawlStarsPlayerResponse(
        String tag,
        String name,
        Integer trophies,
        String rankedRankName
) {
}