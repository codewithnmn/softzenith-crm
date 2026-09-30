package com.softzenith.crm.platform;

import com.softzenith.crm.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

/** A SoftZenith person who onboards and configures tenants. Not a tenant's staff member; signs in with a password. */
@Entity
@Table(name = "platform_admins")
class PlatformAdmin extends AuditableEntity {

    /** Consecutive wrong passwords before the account is locked for {@link #LOCK}. */
    static final int MAX_FAILURES = 5;
    static final Duration LOCK = Duration.ofMinutes(15);

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private int failedAttempts;

    private Instant lockedUntil;
    private Instant lastLoginAt;

    protected PlatformAdmin() {
    }

    PlatformAdmin(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    boolean isLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    void loginFailed(Instant now) {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILURES) {
            lockedUntil = now.plus(LOCK);
            failedAttempts = 0;
        }
    }

    void loginSucceeded(Instant now) {
        failedAttempts = 0;
        lockedUntil = null;
        lastLoginAt = now;
    }

    void changePasswordHash(String hash) {
        this.passwordHash = hash;
    }

    String getUsername() { return username; }
    String getPasswordHash() { return passwordHash; }
    int getFailedAttempts() { return failedAttempts; }
    Instant getLockedUntil() { return lockedUntil; }
    Instant getLastLoginAt() { return lastLoginAt; }
}
