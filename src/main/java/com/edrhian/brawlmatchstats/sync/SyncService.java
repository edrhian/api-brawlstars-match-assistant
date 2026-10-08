package com.edrhian.brawlmatchstats.sync;

import com.edrhian.brawlmatchstats.battle.BattleService;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsApiException;
import com.edrhian.brawlmatchstats.player.PlayerDto;
import com.edrhian.brawlmatchstats.player.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Refreshes the profile of a player and then stores their new battles.
 * Used by the scheduled job and by the "force sync" endpoints.
 *
 * The methods are synchronized, so a manual sync and the scheduled job never run at the same
 * time (two syncs at once could store the same match twice for two tracked teammates).
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final PlayerService playerService;
    private final BattleService battleService;

    public SyncService(PlayerService playerService, BattleService battleService) {
        this.playerService = playerService;
        this.battleService = battleService;
    }

    /**
     * Syncs one player. Errors are thrown to the caller.
     * If the player has tracking disabled, only the profile is refreshed.
     */
    public synchronized PlayerSyncResult syncPlayer(String tag) {
        // First the profile (rank and trophies are saved in each battle), then the battles
        PlayerDto player = playerService.refreshPlayer(tag);
        int newBattles = battleService.fetchAndStoreBattles(tag);
        return new PlayerSyncResult(player, newBattles);
    }

    /**
     * Syncs all the players with tracking enabled, one after the other.
     * A failing player is skipped, so the others still run.
     */
    public synchronized SyncSummary syncTrackedPlayers() {
        List<PlayerDto> players = playerService.getTrackedPlayers();
        log.info("Battle sync started for {} tracked players", players.size());

        int synced = 0;
        int failed = 0;
        int newBattles = 0;
        boolean stoppedEarly = false;

        for (PlayerDto player : players) {
            try {
                newBattles += syncPlayer(player.tag()).newBattles();
                synced++;
            } catch (BrawlStarsApiException e) {
                failed++;
                if (failsForEveryPlayer(e)) {
                    log.error("Battle sync stopped: {}", e.getMessage());
                    stoppedEarly = true;
                    break;
                }
                log.warn("Skipping player {}: {}", player.tag(), e.getMessage());
            } catch (Exception e) {
                // One broken player must not stop the others
                failed++;
                log.warn("Skipping player {}: {}", player.tag(), e.getMessage());
            }
        }

        log.info("Battle sync finished: {} new battles stored", newBattles);
        return new SyncSummary(players.size(), synced, failed, newBattles, stoppedEarly);
    }

    // Bad key or IP (403), rate limit (429) and maintenance (503) will fail for all the players
    private boolean failsForEveryPlayer(BrawlStarsApiException e) {
        int status = e.getStatusCode();
        return status == 403 || status == 429 || status == 503;
    }
}