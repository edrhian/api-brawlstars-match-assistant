package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsClient;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsPlayerResponse;
import com.edrhian.brawlmatchstats.shared.BattleType;
import com.edrhian.brawlmatchstats.shared.TagUtils;
import com.edrhian.brawlmatchstats.shared.TimeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Registers players and keeps their profile data (name, trophies, rank) and tracking settings.
 */
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final BrawlStarsClient brawlStarsClient;

    public PlayerService(PlayerRepository playerRepository, BrawlStarsClient brawlStarsClient) {
        this.playerRepository = playerRepository;
        this.brawlStarsClient = brawlStarsClient;
    }

    /**
     * Adds a new player. The tag is checked against the BrawlStars API, so a wrong tag fails here.
     *
     * @param trackingEnabled null means enabled
     * @param trackingTypes   null means all the battle types
     * @throws PlayerAlreadyExistsException if the player is already registered
     */
    public PlayerDto registerPlayer(String tag, Boolean trackingEnabled, List<BattleType> trackingTypes) {
        String cleanTag = TagUtils.normalize(tag);
        if (playerRepository.existsById(cleanTag)) {
            throw new PlayerAlreadyExistsException(cleanTag);
        }

        BrawlStarsPlayerResponse response = brawlStarsClient.getPlayer(cleanTag);

        Player player = new Player();
        player.setTag(cleanTag);
        player.setTrackingEnabled(trackingEnabled == null || trackingEnabled);
        player.setTrackingTypes(trackingTypes == null ? allTypes() : cleanTypes(trackingTypes));
        applyApiData(player, response);

        return PlayerMapper.toDto(playerRepository.save(player));
    }

    /**
     * Asks the BrawlStars API for the current name, trophies and rank of a registered player.
     */
    public PlayerDto refreshPlayer(String tag) {
        Player player = findPlayerOrThrow(tag);
        applyApiData(player, brawlStarsClient.getPlayer(player.getTag()));

        return PlayerMapper.toDto(playerRepository.save(player));
    }

    public PlayerDto setTrackingEnabled(String tag, boolean trackingEnabled) {
        Player player = findPlayerOrThrow(tag);
        player.setTrackingEnabled(trackingEnabled);

        return PlayerMapper.toDto(playerRepository.save(player));
    }

    public PlayerDto setTrackingTypes(String tag, List<BattleType> trackingTypes) {
        Player player = findPlayerOrThrow(tag);
        player.setTrackingTypes(cleanTypes(trackingTypes));

        return PlayerMapper.toDto(playerRepository.save(player));
    }

    public PlayerDto getPlayer(String tag) {
        return PlayerMapper.toDto(findPlayerOrThrow(tag));
    }

    public List<PlayerDto> getAllPlayers() {
        return playerRepository.findAll().stream().map(PlayerMapper::toDto).toList();
    }

    // The players the scheduled job has to update
    public List<PlayerDto> getTrackedPlayers() {
        return playerRepository.findByTrackingEnabledTrue().stream().map(PlayerMapper::toDto).toList();
    }

    private Player findPlayerOrThrow(String tag) {
        String cleanTag = TagUtils.normalize(tag);
        return playerRepository.findById(cleanTag)
                .orElseThrow(() -> new NoSuchElementException("Player not registered: " + cleanTag));
    }

    private void applyApiData(Player player, BrawlStarsPlayerResponse response) {
        if (response == null) {
            throw new IllegalStateException("The BrawlStars API returned an empty player");
        }
        player.setName(response.name());
        player.setTrophies(response.trophies());
        player.setRankedRank(response.rankedRankName());
        player.setLastUpdated(TimeUtils.nowAsString());
    }

    private List<BattleType> allTypes() {
        return new ArrayList<>(List.of(BattleType.values()));
    }

    // Removes repeated types and never leaves a null list
    private List<BattleType> cleanTypes(List<BattleType> types) {
        if (types == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(types.stream().distinct().toList());
    }
}