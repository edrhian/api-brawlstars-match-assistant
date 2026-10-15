package com.edrhian.brawlmatchstats.analysis;

import com.edrhian.brawlmatchstats.battle.Battle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the WHERE of the battle query from the filters. The conditions are added in the
 * order of the pipeline: players, battle type, time, trophies, rank, map or game mode.
 * The database decides the real execution order using the indexes.
 */
public final class BattleSpecifications {

    private BattleSpecifications() {
        // Only static methods, nobody needs to create this class
    }

    public static Specification<Battle> matching(BattleFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            // 1. Players: a battle counts if the player is Ally 1, 2 or 3 (never as enemy)
            if (!filter.allPlayers()) {
                conditions.add(cb.or(
                        root.get("ally1").in(filter.players()),
                        root.get("ally2").in(filter.players()),
                        root.get("ally3").in(filter.players())));
            }

            // 2. Battle type
            if (!filter.battleTypes().isEmpty()) {
                conditions.add(root.get("battleType").in(filter.battleTypes()));
            }

            // 3. Time frame (the stored format can be compared as text)
            if (filter.fromTime() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.<String>get("battleTime"), filter.fromTime()));
            }
            if (filter.toTime() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.<String>get("battleTime"), filter.toTime()));
            }

            // 4. Trophies
            if (filter.trophiesMin() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.<Integer>get("trophies"), filter.trophiesMin()));
            }
            if (filter.trophiesMax() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.<Integer>get("trophies"), filter.trophiesMax()));
            }

            // 5. Rank
            if (!filter.ranks().isEmpty()) {
                conditions.add(root.get("rank").in(filter.ranks()));
            }

            // 6. Maps override game modes
            if (!filter.maps().isEmpty()) {
                conditions.add(root.get("map").in(filter.maps()));
            } else if (!filter.gameModes().isEmpty()) {
                conditions.add(root.get("gameMode").in(filter.gameModes()));
            }

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}