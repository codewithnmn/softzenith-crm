package com.softzenith.crm.shared.tenant;

import java.util.Set;
import java.util.UUID;

/**
 * Read side of a tenant's feature gates, implemented by the {@code tenancy} module (where {@code TenantSettings}
 * lives) so that {@code shared}, an open module depending on none of the others, can still check entitlements.
 */
public interface TenantFeatureLookup {

    Set<FeatureGate> featuresOf(UUID tenantId);
}
