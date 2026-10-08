package com.edrhian.brawlmatchstats.scheduler;

import com.edrhian.brawlmatchstats.battle.BattleService;
import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsApiException;
import com.edrhian.brawlmatchstats.player.PlayerDto;
import com.edrhian.brawlmatchstats.player.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Every X minutes, refreshes each tracked player and stores their new battles.
 *
 * Set app.sync.enabled=false to turn it off.
 */
@Component
@ConditionalOnProperty(name = "app.sync.enabled", havingValue = "true", matchIfMissing = true)
public class BattleSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(BattleSyncScheduler.class);

    private final PlayerService playerService;
    private final BattleService battleService;

    public BattleSyncScheduler(PlayerService playerService, BattleService battleService) {
        this.playerService = playerService;
        this.battleService = battleService;
    }

    /**
     * fixedDelay waits for the end of the previous run before counting the interval,
     * so two runs can never overlap. The first run starts when the app starts.
     */
    @Scheduled(fixedDelayString = "${app.sync.interval-minutes:15}", timeUnit = TimeUnit.MINUTES)
    public void syncTrackedPlayers() {
        List<PlayerDto> players = playerService.getTrackedPlayers();
        log.info("Battle sync started for {} tracked players", players.size());

        int totalSaved = 0;
        for (PlayerDto player : players) {
            try {
                // First the profile (rank and trophies are saved in each battle), then the battles
                playerService.refreshPlayer(player.tag());
                totalSaved += battleService.fetchAndStoreBattles(player.tag());
            } catch (BrawlStarsApiException e) {
                if (failsForEveryPlayer(e)) {
                    log.error("Battle sync stopped: {}", e.getMessage());
                    return;
                }
                log.warn("Skipping player {}: {}", player.tag(), e.getMessage());
            } catch (Exception e) {
                // One broken player must not stop the others
                log.warn("Skipping player {}: {}", player.tag(), e.getMessage());
            }
        }

        log.info("Battle sync finished: {} new battles stored", totalSaved);
    }

    // Bad key or IP (403), rate limit (429) and maintenance (503) will fail for all the players
    private boolean failsForEveryPlayer(BrawlStarsApiException e) {
        int status = e.getStatusCode();
        return status == 403 || status == 429 || status == 503;
    }
}