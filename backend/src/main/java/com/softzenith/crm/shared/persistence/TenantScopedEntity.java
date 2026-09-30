package com.softzenith.crm.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/**
 * Base for every row that belongs to a tenant. Hibernate fills {@code tenant_id} from
 * {@code TenantContext} on insert and adds {@code tenant_id = :current} to every query.
 */
@MappedSuperclass
public abstract class TenantScopedEntity extends AuditableEntity {

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    protected TenantScopedEntity() {
    }

    /** Only for rows created in system context (e.g. tenant onboarding); elsewhere the context supplies it. */
    protected TenantScopedEntity(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTenantId() { return tenantId; }
}
