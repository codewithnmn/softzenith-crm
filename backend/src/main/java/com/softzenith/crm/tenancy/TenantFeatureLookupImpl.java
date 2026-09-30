package com.softzenith.crm.tenancy;

import com.softzenith.crm.shared.tenant.FeatureGate;
import com.softzenith.crm.shared.tenant.TenantFeatureLookup;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/** Backs the shared kernel's feature-gate check with the tenant's {@code settings.enabledFeatures}. */
@Component
@Transactional(readOnly = true)
class TenantFeatureLookupImpl implements TenantFeatureLookup {

    private final TenantRepository tenants;

    TenantFeatureLookupImpl(TenantRepository tenants) {
        this.tenants = tenants;
    }

    @Override
    public Set<FeatureGate> featuresOf(UUID tenantId) {
        return tenants.findById(tenantId).map(t -> t.getSettings().enabledFeatures()).orElse(Set.of());
    }
}
