package com.softzenith.crm.platform;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAdminTest {

    @Test
    void locksAfterFiveWrongPasswordsForFifteenMinutes() {
        var admin = new PlatformAdmin("softzenith", "hash");
        var now = Instant.parse("2026-09-30T10:00:00Z");
        for (int i = 0; i < 4; i++) admin.loginFailed(now);
        assertThat(admin.isLocked(now)).isFalse();

        admin.loginFailed(now);
        assertThat(admin.isLocked(now)).isTrue();
        assertThat(admin.isLocked(now.plus(PlatformAdmin.LOCK).minusSeconds(1))).isTrue();
        assertThat(admin.isLocked(now.plus(PlatformAdmin.LOCK))).isFalse();
    }

    @Test
    void aSuccessfulSignInClearsFailures() {
        var admin = new PlatformAdmin("softzenith", "hash");
        var now = Instant.now();
        for (int i = 0; i < 4; i++) admin.loginFailed(now);
        admin.loginSucceeded(now);
        assertThat(admin.getFailedAttempts()).isZero();
        assertThat(admin.getLastLoginAt()).isEqualTo(now);
        admin.loginFailed(now);
        assertThat(admin.isLocked(now)).isFalse(); // counting starts again
    }
}
