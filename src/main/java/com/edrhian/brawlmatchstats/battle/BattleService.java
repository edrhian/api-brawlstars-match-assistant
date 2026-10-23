package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsBattleLogResponse.Participant;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsClient;
import com.edrhian.brawlmatchstats.player.Player;
import com.edrhian.brawlmatchstats.player.PlayerRepository;
import com.edrhian.brawlmatchstats.shared.BattleType;
import com.edrhian.brawlmatchstats.shared.TagUtils;
import com.edrhian.brawlmatchstats.shared.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Reads the battle log of a tracked player and stores the battles that we do not have yet.
 *
 * There is no @Transactional on purpose: the database has one connection (SQLite), and we do
 * not want to keep it busy while waiting for the BrawlStars API. Each save is its own transaction.
 */
@Service
public class BattleService {

    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    private final BattleRepository battleRepository;
    private final PlayerRepository playerRepository;
    private final BrawlStarsClient brawlStarsClient;

    public BattleService(BattleRepository battleRepository,
                         PlayerRepository playerRepository,
                         BrawlStarsClient brawlStarsClient) {
        this.battleRepository = battleRepository;
        this.playerRepository = playerRepository;
        this.brawlStarsClient = brawlStarsClient;
    }

    /**
     * Gets the last battles of a registered player and stores the new ones.
     *
     * @return how many battles were stored
     */
    public int fetchAndStoreBattles(String tag) {
        String cleanTag = TagUtils.normalize(tag);
        Player player = playerRepository.findById(cleanTag)
                .orElseThrow(() -> new NoSuchElementException("Player not registered: " + cleanTag));

        if (!player.isTrackingEnabled()) {
            return 0;
        }

        int saved = 0;
        for (BrawlStarsBattleLogResponse.Item item : brawlStarsClient.getBattleLog(cleanTag)) {
            if (saveIfNeeded(player, item)) {
                saved++;
            }
        }

        log.info("Player {}: {} new battles stored", cleanTag, saved);
        return saved;
    }

    private boolean saveIfNeeded(Player player, BrawlStarsBattleLogResponse.Item item) {
        // 1. Battle type filter: unknown types and types the player does not track are skipped
        BattleType type = parseBattleType(item);
        if (type == null) {
            log.debug("Skipped {} of {}: battle type '{}' is not supported",
                    item.battleTime(), player.getTag(), item.battle() == null ? null : item.battle().type());
            return false;
        }
        if (!player.getTrackingTypes().contains(type)) {
            log.debug("Skipped {} of {}: the player does not track {}", item.battleTime(), player.getTag(), type);
            return false;
        }

        // 2. Build the battle (null if it has no two teams or the tracked player is not in them)
        Battle battle = buildBattle(player, item, type);
        if (battle == null) {
            log.debug("Skipped {} of {}: there are no two teams, or the player is not in them",
                    item.battleTime(), player.getTag());
            return false;
        }

        // 3. Duplicate check: the ID is the same whichever of the 6 players reads the battle,
        //    so if any player already stored it (ally or enemy), the ID exists
        if (battleRepository.existsById(battle.getId())) {
            log.debug("Skipped {}: already stored", battle.getId());
            return false;
        }

        battleRepository.save(battle);
        return true;
    }

    // Returns null when the API type is not one of ours (trophyRoad, challenge...)
    private BattleType parseBattleType(BrawlStarsBattleLogResponse.Item item) {
        if (item.battle() == null || item.battle().type() == null) {
            return null;
        }
        try {
            return BattleType.valueOf(item.battle().type());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Battle buildBattle(Player player, BrawlStarsBattleLogResponse.Item item, BattleType type) {
        List<List<Participant>> teams = item.battle().teams();

        // Modes without two teams (like Showdown) are not supported
        if (item.battleTime() == null || teams == null || teams.size() != 2) {
            return null;
        }

        String tag = player.getTag();
        List<Participant> teamA = cleanTeam(teams.get(0));
        List<Participant> teamB = cleanTeam(teams.get(1));

        // The tracked player can be in any of the two teams, so we look for it
        List<Participant> allyTeam;
        List<Participant> enemyTeam;
        if (findByTag(teamA, tag) != null) {
            allyTeam = teamA;
            enemyTeam = teamB;
        } else if (findByTag(teamB, tag) != null) {
            allyTeam = teamB;
            enemyTeam = teamA;
        } else {
            return null;
        }

        Participant me = findByTag(allyTeam, tag);
        List<Participant> mates = allyTeam.stream().filter(p -> !tag.equals(p.tag())).toList();

        Battle battle = new Battle();
        battle.setBattleTime(item.battleTime());
        battle.setBattleType(type);
        battle.setRank(player.getRankedRank());
        battle.setTrophies(player.getTrophies());
        battle.setGameMode(item.event() != null && item.event().mode() != null
                ? item.event().mode() : item.battle().mode());
        battle.setMap(item.event() != null ? item.event().map() : null);

        // Ally 1 is the player whose log we read, the other two are the rest of the team
        battle.setAlly1(tag);
        battle.setAlly1Brawler(brawlerOf(me));
        battle.setAlly2(tagOf(at(mates, 0)));
        battle.setAlly2Brawler(brawlerOf(at(mates, 0)));
        battle.setAlly3(tagOf(at(mates, 1)));
        battle.setAlly3Brawler(brawlerOf(at(mates, 1)));

        battle.setEnemy1(tagOf(at(enemyTeam, 0)));
        battle.setEnemy1Brawler(brawlerOf(at(enemyTeam, 0)));
        battle.setEnemy2(tagOf(at(enemyTeam, 1)));
        battle.setEnemy2Brawler(brawlerOf(at(enemyTeam, 1)));
        battle.setEnemy3(tagOf(at(enemyTeam, 2)));
        battle.setEnemy3Brawler(brawlerOf(at(enemyTeam, 2)));

        // The ID uses the 6 tags, so it must be built after all the slots are filled
        battle.setId(Battle.buildId(item.battleTime(),
                battle.getAlly1(), battle.getAlly2(), battle.getAlly3(),
                battle.getEnemy1(), battle.getEnemy2(), battle.getEnemy3()));

        battle.setBattleResult(item.battle().result());
        battle.setFetchedAt(TimeUtils.nowAsString());
        return battle;
    }

    // Removes null team or null players, so the rest of the code does not need null checks
    private List<Participant> cleanTeam(List<Participant> team) {
        if (team == null) {
            return List.of();
        }
        return team.stream().filter(Objects::nonNull).toList();
    }

    private Participant findByTag(List<Participant> team, String tag) {
        return team.stream().filter(p -> tag.equals(p.tag())).findFirst().orElse(null);
    }

    private Participant at(List<Participant> team, int index) {
        return index < team.size() ? team.get(index) : null;
    }

    private String tagOf(Participant participant) {
        return participant == null ? null : participant.tag();
    }

    private String brawlerOf(Participant participant) {
        return participant == null || participant.brawler() == null ? null : participant.brawler().name();
    }
}