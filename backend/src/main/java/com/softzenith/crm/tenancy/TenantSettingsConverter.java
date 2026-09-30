package com.softzenith.crm.tenancy;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
class TenantSettingsConverter implements AttributeConverter<TenantSettings, String> {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(TenantSettings settings) {
        return JSON.writeValueAsString(settings == null ? TenantSettings.defaults() : settings);
    }

    @Override
    public TenantSettings convertToEntityAttribute(String json) {
        return json == null || json.isBlank() ? TenantSettings.defaults() : JSON.readValue(json, TenantSettings.class);
    }
}
