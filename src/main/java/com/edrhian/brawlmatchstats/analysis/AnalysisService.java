package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import com.edrhian.brawlmatchstats.battle.BattleRange;
import com.edrhian.brawlmatchstats.battle.BattleRepository;
import com.edrhian.brawlmatchstats.player.Player;
import com.edrhian.brawlmatchstats.player.PlayerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Battle analysis. Two steps:
 * 1. findBattles: the database applies all the filters (the "filtered battles").
 * 2. the calculator counts the results of those battles.
 */
@Service
public class AnalysisService {

    private final BattleRepository battleRepository;
    private final PlayerRepository playerRepository;

    public AnalysisService(BattleRepository battleRepository, PlayerRepository playerRepository) {
        this.battleRepository = battleRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Winrate of each Brawler, seen from its own team (an enemy win is a win for the enemy Brawlers).
     * "with" and "against" are applied after the limit, to the filtered battles.
     */
    public BrawlerAnalysisResponse analyzeBrawlers(BattleFilter filter, BrawlerInput input, int minGames) {
        List<Battle> battles = findBattles(filter);
        return new BrawlerAnalysisResponse(
                battles.size(),
                BrawlerStatsCalculator.brawlers(battles, input, minGames));
    }

    /**
     * Existing values of the database for some players (all their battles, without other filters).
     *
     * @param players already normalized tags (ignored when allPlayers is true)
     */
    public FilterOptionsResponse getFilterOptions(List<String> players, boolean allPlayers) {
        List<String> tags;
        if (allPlayers) {
            tags = playerRepository.findAll().stream().map(Player::getTag).toList();
            if (tags.isEmpty()) {
                return new FilterOptionsResponse(List.of(), List.of(), List.of(), List.of(), null, null, null, null);
            }
        } else {
            requireRegistered(players);
            tags = players;
        }

        BattleRange range = battleRepository.findRangeByPlayers(tags);
        return new FilterOptionsResponse(
                battleRepository.findDistinctBattleTypes(tags),
                battleRepository.findDistinctRanks(tags),
                battleRepository.findDistinctGameModes(tags),
                battleRepository.findDistinctMaps(tags),
                range.minTrophies(),
                range.maxTrophies(),
                BattleTimes.toIso(range.firstBattleTime()),
                BattleTimes.toIso(range.lastBattleTime()));
    }

    /**
     * The battles that pass all the filters. The limit is the last step: the N most recent
     * battles are taken from the battles that already passed the other filters.
     */
    public List<Battle> findBattles(BattleFilter filter) {
        if (!filter.allPlayers()) {
            requireRegistered(filter.players());
        }

        Specification<Battle> specification = BattleSpecifications.matching(filter);
        if (filter.limit() == null) {
            return battleRepository.findAll(specification);
        }

        Pageable newestFirst = PageRequest.of(0, filter.limit(),
                Sort.by(Sort.Order.desc("battleTime"), Sort.Order.desc("id")));
        return battleRepository.findAll(specification, newestFirst).getContent();
    }

    private void requireRegistered(List<String> tags) {
        List<String> registered = playerRepository.findAllById(tags).stream().map(Player::getTag).toList();
        List<String> missing = tags.stream().filter(tag -> !registered.contains(tag)).toList();
        if (!missing.isEmpty()) {
            throw new NoSuchElementException("Players not registered: " + String.join(", ", missing));
        }
    }
}