package com.softzenith.crm.tenancy;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Gap-tolerant, concurrency-safe per-tenant sequences for human-readable numbers (lead numbers now,
 * student / application numbers later). One upsert row per (tenant, counter name).
 */
@Service
public class TenantCounters {

    private final EntityManager em;

    TenantCounters(EntityManager em) {
        this.em = em;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public long next(UUID tenantId, String name) {
        return ((Number) em.createNativeQuery("""
                        insert into tenant_counters (tenant_id, name, value) values (:tenant, :name, 1)
                        on conflict (tenant_id, name) do update set value = tenant_counters.value + 1
                        returning value""")
                .setParameter("tenant", tenantId)
                .setParameter("name", name)
                .getSingleResult()).longValue();
    }
}
