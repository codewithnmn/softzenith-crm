package com.softzenith.crm.tenancy;

import com.softzenith.crm.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;

/** A customer business using the platform (e.g. Western World). The isolation boundary for all data. */
@Entity
@Table(name = "tenants")
public class Tenant extends AuditableEntity {

    public enum Status { ACTIVE, SUSPENDED }

    @Column(nullable = false, updatable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    /** ISO 3166 region used to parse phone numbers typed without a country code. */
    @Column(nullable = false)
    private String defaultRegion;

    @Column(nullable = false)
    private String timezone;

    @Convert(converter = TenantSettingsConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(nullable = false, columnDefinition = "jsonb")
    private TenantSettings settings = TenantSettings.defaults();

    protected Tenant() {
    }

    public Tenant(String slug, String name, String defaultRegion, String timezone) {
        this.slug = slug;
        this.name = name;
        this.defaultRegion = defaultRegion;
        this.timezone = timezone;
    }

    public void updateSettings(TenantSettings settings) {
        this.settings = settings == null ? TenantSettings.defaults() : settings;
    }

    public String getSlug() { return slug; }
    public String getName() { return name; }
    public Status getStatus() { return status; }
    public String getDefaultRegion() { return defaultRegion; }
    public String getTimezone() { return timezone; }
    public TenantSettings getSettings() { return settings; }
}
