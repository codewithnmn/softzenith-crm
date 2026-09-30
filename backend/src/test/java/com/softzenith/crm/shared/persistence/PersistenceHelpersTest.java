package com.softzenith.crm.shared.persistence;

import com.softzenith.crm.identity.Branch;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The custom-fields JSON converter and entity identity, without a database. */
class PersistenceHelpersTest {

    private final JsonMapConverter converter = new JsonMapConverter();

    @Test
    void customFieldsRoundTripAndKeepTheirOrder() {
        var fields = new java.util.LinkedHashMap<String, Object>();
        fields.put("b", 1);
        fields.put("a", "x");
        var json = converter.convertToDatabaseColumn(fields);
        assertThat(converter.convertToEntityAttribute(json)).containsExactly(Map.entry("b", 1), Map.entry("a", "x"));
    }

    @Test
    void missingCustomFieldsAreAnEmptyObject() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("{}");
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        var blank = converter.convertToEntityAttribute("");
        assertThat(blank).isInstanceOf(java.util.LinkedHashMap.class);
        assertThat(blank).isEmpty();
    }

    @Test
    void unsavedEntitiesAreOnlyEqualToThemselves() {
        var a = new Branch("Rohtak", null);
        var b = new Branch("Rohtak", null);
        assertThat(a).isEqualTo(a).isNotEqualTo(b).isNotEqualTo(null).isNotEqualTo("Rohtak");
        assertThat(a.hashCode()).isEqualTo(b.hashCode()); // stable across the id being assigned on save
        assertThat(a.getId()).isNull();
        assertThat(a.getUpdatedAt()).isNull();
        assertThat(a.getUpdatedBy()).isNull();
        assertThat(a.getVersion()).isZero();
    }
}
