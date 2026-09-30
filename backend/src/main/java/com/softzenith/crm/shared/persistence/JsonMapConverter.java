package com.softzenith.crm.shared.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps a JSONB {@code custom_fields} column to a Map. Pair with {@code @ColumnTransformer(write = "?::jsonb")}.
 * Tenant-specific fields live here until every tenant needs them and they are promoted to real columns.
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<LinkedHashMap<String, Object>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<String, Object> value) {
        return JSON.writeValueAsString(value == null ? Map.of() : value);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String json) {
        return json == null || json.isBlank() ? new LinkedHashMap<>() : JSON.readValue(json, TYPE);
    }
}
