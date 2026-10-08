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
 * 2. a calculator counts the results of those battles.
 *
 * Every analysis reuses step 1 and only changes the calculator.
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
     * Pick comfort: results of the ally team for each Brawler picked in the ally team.
     */
    public BrawlerAnalysisResponse analyzeAllyBrawlers(BattleFilter filter, int minGames) {
        List<Battle> battles = findBattles(filter);
        return new BrawlerAnalysisResponse(
                BrawlerStatsCalculator.overall(battles),
                BrawlerStatsCalculator.allyBrawlers(battles, minGames));
    }

    /**
     * Confrontation comfort: results of the ally team for each Brawler seen in the enemy team.
     * winrate is how often we win against it, loserate how often we lose.
     */
    public BrawlerAnalysisResponse analyzeEnemyBrawlers(BattleFilter filter, int minGames) {
        List<Battle> battles = findBattles(filter);
        return new BrawlerAnalysisResponse(
                BrawlerStatsCalculator.overall(battles),
                BrawlerStatsCalculator.enemyBrawlers(battles, minGames));
    }

    /**
     * Ally synergy: results of each partner Brawler in the battles where all the input Brawlers
     * were in the ally team. "withInput" is the result of those battles without looking at partners.
     */
    public InputAnalysisResponse analyzeAllySynergy(BattleFilter filter, List<String> brawlers, int minGames) {
        List<Battle> battles = findBattles(filter);
        return new InputAnalysisResponse(
                brawlers,
                BrawlerStatsCalculator.overall(battles),
                BrawlerStatsCalculator.overall(BrawlerStatsCalculator.withAllyBrawlers(battles, brawlers)),
                BrawlerStatsCalculator.allyPartners(battles, brawlers, minGames));
    }

    /**
     * Counter analysis: results of each ally pick in the battles where all the input Brawlers
     * were in the enemy team. "withInput" is the result of those battles without looking at picks.
     */
    public InputAnalysisResponse analyzeEnemyCounters(BattleFilter filter, List<String> brawlers, int minGames) {
        List<Battle> battles = findBattles(filter);
        return new InputAnalysisResponse(
                brawlers,
                BrawlerStatsCalculator.overall(battles),
                BrawlerStatsCalculator.overall(BrawlerStatsCalculator.withEnemyBrawlers(battles, brawlers)),
                BrawlerStatsCalculator.countersAgainst(battles, brawlers, minGames));
    }

    /**
     * Existing values of the database for some players (all their battles, without other filters).
     */
    public FilterOptionsResponse getFilterOptions(List<String> players) {
        List<String> tags = AnalysisRequest.normalizeTags(players);
        requireRegistered(tags);

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
        requireRegistered(filter.players());

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