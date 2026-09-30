package com.softzenith.crm.identity;

import com.softzenith.crm.shared.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A physical or logical office of the tenant. Minimal in Phase 1; enriched in Phase 2. */
@Entity
@Table(name = "branches")
public class Branch extends TenantScopedEntity {

    @Column(nullable = false)
    private String name;

    private String city;

    @Column(nullable = false)
    private boolean active = true;

    protected Branch() {
    }

    public Branch(String name, String city) {
        this.name = name;
        this.city = city;
    }

    /** For tenant onboarding, which runs in system context (see {@link OrganisationProvisioner}). */
    Branch(java.util.UUID tenantId, String name, String city) {
        super(tenantId);
        this.name = name;
        this.city = city;
    }

    public void update(String name, String city, boolean active) {
        this.name = name;
        this.city = city;
        this.active = active;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public boolean isActive() { return active; }
}
