package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReadsPlayerWithTrackingTypes() {
        playerRepository.save(createPlayer("#2VU8PGCC", true, BattleType.ranked, BattleType.soloRanked));

        // Send the data to the database and forget it, so the next read comes from SQLite
        entityManager.flush();
        entityManager.clear();

        Optional<Player> found = playerRepository.findById("#2VU8PGCC");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("edrhian27");
        assertThat(found.get().getTrophies()).isEqualTo(111557);
        assertThat(found.get().getRankedRank()).isEqualTo("LEGENDARY I");
        assertThat(found.get().isTrackingEnabled()).isTrue();
        assertThat(found.get().getTrackingTypes()).containsExactly(BattleType.ranked, BattleType.soloRanked);
        assertThat(found.get().getLastUpdated()).isEqualTo("2026-10-07T11:00:39");
    }

    @Test
    void savesPlayerWithoutTrackingTypes() {
        playerRepository.save(createPlayer("#P0PULRC", false));

        entityManager.flush();
        entityManager.clear();

        Player found = playerRepository.findById("#P0PULRC").orElseThrow();

        assertThat(found.getTrackingTypes()).isEmpty();
    }

    @Test
    void findsOnlyPlayersWithTrackingEnabled() {
        playerRepository.save(createPlayer("#2VU8PGCC", true, BattleType.ranked));
        playerRepository.save(createPlayer("#P0PULRC", false, BattleType.friendly));

        List<Player> tracked = playerRepository.findByTrackingEnabledTrue();

        assertThat(tracked).extracting(Player::getTag).containsExactly("#2VU8PGCC");
    }

    private Player createPlayer(String tag, boolean trackingEnabled, BattleType... types) {
        Player player = new Player();
        player.setTag(tag);
        player.setName("edrhian27");
        player.setTrophies(111557);
        player.setRankedRank("LEGENDARY I");
        player.setTrackingEnabled(trackingEnabled);
        player.setTrackingTypes(List.of(types));
        player.setLastUpdated("2026-10-07T11:00:39");
        return player;
    }
}