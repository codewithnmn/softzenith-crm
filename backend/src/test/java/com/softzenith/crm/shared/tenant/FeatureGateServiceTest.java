package com.softzenith.crm.shared.tenant;

import com.softzenith.crm.shared.web.FeatureNotEntitledException;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeatureGateServiceTest {

    private final UUID entitledTenant = UUID.randomUUID();
    private final UUID restrictedTenant = UUID.randomUUID();

    private final FeatureGateService gate = new FeatureGateService(tenantId ->
            tenantId.equals(entitledTenant) ? Set.of(FeatureGate.LEADS_CORE, FeatureGate.TEAM_AND_STUDENTS) : Set.of());

    @Test
    void hasFeatureReflectsTheCurrentTenantsEntitlements() {
        TenantContext.run(entitledTenant, () -> {
            assertThat(gate.hasFeature(FeatureGate.LEADS_CORE)).isTrue();
            assertThat(gate.hasFeature(FeatureGate.ENGAGEMENT)).isFalse();
        });
        TenantContext.run(restrictedTenant, () -> assertThat(gate.hasFeature(FeatureGate.LEADS_CORE)).isFalse());
    }

    @Test
    void requireFeaturePassesSilentlyWhenEntitled() {
        TenantContext.run(entitledTenant, () -> gate.requireFeature(FeatureGate.TEAM_AND_STUDENTS));
    }

    @Test
    void requireFeatureThrowsWhenNotEntitled() {
        TenantContext.run(restrictedTenant, () ->
                assertThatThrownBy(() -> gate.requireFeature(FeatureGate.TEAM_AND_STUDENTS))
                        .isInstanceOf(FeatureNotEntitledException.class));
    }

    @Test
    void requiresATenantInContext() {
        assertThatThrownBy(() -> gate.hasFeature(FeatureGate.LEADS_CORE)).isInstanceOf(IllegalStateException.class);
    }
}
