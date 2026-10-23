package com.edrhian.brawlmatchstats.battle;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BattleRepositoryTest {

    private static final String BATTLE_TIME = "20261007T070930.000Z";
    private static final String PLAYER_TAG = "#2VU8PGCC";

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReadsBattle() {
        battleRepository.save(createBattle(BATTLE_TIME, PLAYER_TAG, BattleType.soloRanked));

        entityManager.flush();
        entityManager.clear();

        Battle found = battleRepository.findById(Battle.buildId(BATTLE_TIME, PLAYER_TAG)).orElseThrow();

        assertThat(found.getId()).isEqualTo("20261007T070930.000Z_#2VU8PGCC");
        assertThat(found.getBattleTime()).isEqualTo(BATTLE_TIME);
        assertThat(found.getBattleType()).isEqualTo(BattleType.soloRanked);
        assertThat(found.getGameMode()).isEqualTo("heist");
        assertThat(found.getMap()).isEqualTo("Kaboom Canyon");
        assertThat(found.getAlly1Brawler()).isEqualTo("DARRYL");
        assertThat(found.getAlly2()).isEqualTo("#P0R9Y9J09");
        assertThat(found.getEnemy3Brawler()).isEqualTo("BYRON");
        assertThat(found.getBattleResult()).isEqualTo("victory");
        assertThat(found.getFetchedAt()).isEqualTo("2026-10-07T11:00:39");
    }

    private Battle createBattle(String battleTime, String ally1, BattleType type) {
        Battle battle = new Battle();
        battle.setId(Battle.buildId(battleTime, ally1));
        battle.setBattleTime(battleTime);
        battle.setBattleType(type);
        battle.setRank("LEGENDARY I");
        battle.setTrophies(111557);
        battle.setGameMode("heist");
        battle.setMap("Kaboom Canyon");
        battle.setAlly1(ally1);
        battle.setAlly1Brawler("DARRYL");
        battle.setAlly2("#P0R9Y9J09");
        battle.setAlly2Brawler("COLT");
        battle.setAlly3("#P2J88GQG");
        battle.setAlly3Brawler("AMBER");
        battle.setEnemy1("#9J8PRJRQL");
        battle.setEnemy1Brawler("BROCK");
        battle.setEnemy2("#2R0P8LPR9");
        battle.setEnemy2Brawler("CARL");
        battle.setEnemy3("#Y8Q8UPCGV");
        battle.setEnemy3Brawler("BYRON");
        battle.setBattleResult("victory");
        battle.setFetchedAt("2026-10-07T11:00:39");
        return battle;
    }
}