package com.edrhian.brawlmatchstats.player;

import com.edrhian.brawlmatchstats.shared.BattleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite cannot store a list in one column, so we save the list as text.
 *
 * Example: [ranked, soloRanked] <-> "ranked,soloRanked"
 */
@Converter
public class TrackingTypesConverter implements AttributeConverter<List<BattleType>, String> {

    private static final String SEPARATOR = ",";

    // Java -> database
    @Override
    public String convertToDatabaseColumn(List<BattleType> types) {
        if (types == null || types.isEmpty()) {
            return "";
        }
        return String.join(SEPARATOR, types.stream().map(BattleType::name).toList());
    }

    // Database -> Java
    @Override
    public List<BattleType> convertToEntityAttribute(String text) {
        List<BattleType> types = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return types;
        }
        for (String part : text.split(SEPARATOR)) {
            types.add(BattleType.valueOf(part.trim()));
        }
        return types;
    }
}