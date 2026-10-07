package com.edrhian.brawlmatchstats.battle;

/**
 * Converts between the Battle entity and BattleDto.
 */
public final class BattleMapper {

    private BattleMapper() {
        // Only static methods, nobody needs to create this class
    }

    public static BattleDto toDto(Battle battle) {
        return new BattleDto(
                battle.getId(),
                battle.getBattleTime(),
                battle.getBattleType(),
                battle.getRank(),
                battle.getTrophies(),
                battle.getGameMode(),
                battle.getMap(),
                battle.getAlly1(),
                battle.getAlly1Brawler(),
                battle.getAlly2(),
                battle.getAlly2Brawler(),
                battle.getAlly3(),
                battle.getAlly3Brawler(),
                battle.getEnemy1(),
                battle.getEnemy1Brawler(),
                battle.getEnemy2(),
                battle.getEnemy2Brawler(),
                battle.getEnemy3(),
                battle.getEnemy3Brawler(),
                battle.getBattleResult(),
                battle.getFetchedAt()
        );
    }

    public static Battle toEntity(BattleDto dto) {
        Battle battle = new Battle();

        // If the DTO has no ID, we build it from battleTime + ally1
        String id = dto.id() != null ? dto.id() : Battle.buildId(dto.battleTime(), dto.ally1());
        battle.setId(id);

        battle.setBattleTime(dto.battleTime());
        battle.setBattleType(dto.battleType());
        battle.setRank(dto.rank());
        battle.setTrophies(dto.trophies());
        battle.setGameMode(dto.gameMode());
        battle.setMap(dto.map());
        battle.setAlly1(dto.ally1());
        battle.setAlly1Brawler(dto.ally1Brawler());
        battle.setAlly2(dto.ally2());
        battle.setAlly2Brawler(dto.ally2Brawler());
        battle.setAlly3(dto.ally3());
        battle.setAlly3Brawler(dto.ally3Brawler());
        battle.setEnemy1(dto.enemy1());
        battle.setEnemy1Brawler(dto.enemy1Brawler());
        battle.setEnemy2(dto.enemy2());
        battle.setEnemy2Brawler(dto.enemy2Brawler());
        battle.setEnemy3(dto.enemy3());
        battle.setEnemy3Brawler(dto.enemy3Brawler());
        battle.setBattleResult(dto.battleResult());
        battle.setFetchedAt(dto.fetchedAt());
        return battle;
    }
}