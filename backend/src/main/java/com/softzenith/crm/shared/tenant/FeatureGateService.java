package com.softzenith.crm.shared.tenant;

import com.softzenith.crm.shared.web.FeatureNotEntitledException;
import org.springframework.stereotype.Service;

/**
 * Whether the current tenant's plan includes a given {@link FeatureGate}. Orthogonal to permissions:
 * a role's permissions decide who inside the tenant may act, this decides whether the tenant is entitled at all.
 */
@Service
public class FeatureGateService {

    private final TenantFeatureLookup features;

    FeatureGateService(TenantFeatureLookup features) {
        this.features = features;
    }

    public boolean hasFeature(FeatureGate gate) {
        return features.featuresOf(TenantContext.require()).contains(gate);
    }

    public void requireFeature(FeatureGate gate) {
        if (!hasFeature(gate)) {
            throw new FeatureNotEntitledException(gate);
        }
    }
}
