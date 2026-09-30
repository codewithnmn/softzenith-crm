package com.softzenith.crm.platform;

import com.softzenith.crm.onboarding.TenantOnboardingService.Onboarded;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/** The platform module's entry point for its web layer: sign-in and the audit trail of platform actions. */
@Service
public class PlatformSessions {

    public record Session(String username, String accessToken, Instant expiresAt) {
    }

    private final PlatformAuthService auth;
    private final PlatformAudit audit;

    PlatformSessions(PlatformAuthService auth, PlatformAudit audit) {
        this.auth = auth;
        this.audit = audit;
    }

    /** Empty when refused; why is recorded in the platform audit log only. */
    public Optional<Session> login(String username, String password, String clientIp) {
        return auth.login(username, password, clientIp)
                .map(t -> new Session(username.strip().toLowerCase(java.util.Locale.ROOT), t.accessToken(), t.expiresAt()));
    }

    @Transactional
    public void recordOnboarded(String admin, Onboarded tenant) {
        audit.record(admin, "TENANT_ONBOARDED", tenant.slug(),
                tenant.name() + ": " + tenant.branches() + " branches, " + tenant.staff() + " staff invited");
    }
}
