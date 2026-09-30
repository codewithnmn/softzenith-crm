package com.softzenith.crm.shared.web;

import com.softzenith.crm.shared.tenant.FeatureGate;

/** The tenant's plan does not include this feature. Distinct from a permission failure, which is per-user, not per-tenant. */
public class FeatureNotEntitledException extends RuntimeException {

    public FeatureNotEntitledException(FeatureGate gate) {
        super("Your plan does not include this feature (" + gate + ")");
    }
}
