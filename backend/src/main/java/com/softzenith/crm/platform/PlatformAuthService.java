package com.softzenith.crm.platform;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.softzenith.crm.shared.web.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Platform-admin sign-in: bcrypt passwords, account lock after {@value PlatformAdmin#MAX_FAILURES} wrong passwords,
 * a per-address attempt limit, and the same answer (and similar timing) for an unknown user, a wrong password and a
 * locked account, so the form reveals nothing about which usernames exist.
 */
@Service
@Transactional
class PlatformAuthService {

    static final int MIN_PASSWORD_LENGTH = 12;

    private static final Logger log = LoggerFactory.getLogger(PlatformAuthService.class);

    private final PlatformAdminRepository admins;
    private final PlatformTokens tokens;
    private final PlatformAudit audit;
    private final PasswordEncoder passwords = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    /** Checked when the username is unknown, so that case costs as long as a wrong password. */
    private final String dummyHash = passwords.encode(UUID.randomUUID().toString());
    private final Cache<String, Bucket> attempts = Caffeine.newBuilder().maximumSize(10_000)
            .expireAfterAccess(Duration.ofHours(1)).build();

    PlatformAuthService(PlatformAdminRepository admins, PlatformTokens tokens, PlatformAudit audit) {
        this.admins = admins;
        this.tokens = tokens;
        this.audit = audit;
    }

    /** Empty when the sign-in is refused (the reason is only in the audit log). */
    Optional<PlatformTokens.Issued> login(String rawUsername, String password, String clientIp) {
        limit(clientIp);
        var username = normalise(rawUsername);
        var now = Instant.now();
        var found = admins.findByUsername(username);
        if (found.isEmpty()) {
            passwords.matches(password, dummyHash);
            audit.record(username, "LOGIN_REFUSED", null, "unknown username from " + clientIp);
            return Optional.empty();
        }
        var admin = found.get();
        if (admin.isLocked(now)) {
            audit.record(username, "LOGIN_REFUSED", null, "account locked until " + admin.getLockedUntil());
            return Optional.empty();
        }
        if (!passwords.matches(password, admin.getPasswordHash())) {
            admin.loginFailed(now);
            audit.record(username, "LOGIN_REFUSED", null, admin.isLocked(now)
                    ? "wrong password; account locked for " + PlatformAdmin.LOCK.toMinutes() + " min" : "wrong password");
            return Optional.empty();
        }
        if (passwords.upgradeEncoding(admin.getPasswordHash())) {
            admin.changePasswordHash(passwords.encode(password));
        }
        admin.loginSucceeded(now);
        audit.record(username, "LOGIN", null, "from " + clientIp);
        return Optional.of(tokens.issue(username));
    }

    /** Creates the first admin; used by {@link PlatformAdminBootstrap}. */
    void createAdmin(String rawUsername, String password) {
        var username = normalise(rawUsername);
        if (!username.matches("^[a-z0-9._-]{3,50}$")) {
            throw new IllegalStateException("Platform admin username: 3–50 lower-case letters, digits, '.', '_' or '-'");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("Platform admin password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        admins.save(new PlatformAdmin(username, passwords.encode(password)));
        audit.record("system", "ADMIN_CREATED", username, "bootstrap from configuration");
        log.warn("Created platform admin '{}'. Remove the bootstrap password from the environment now.", username);
    }

    boolean anyAdminExists() {
        return admins.count() > 0;
    }

    private void limit(String clientIp) {
        var bucket = attempts.get(clientIp, ip -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(10)).build()).build());
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            log.warn("Platform sign-in attempts from {} throttled", clientIp);
            throw new TooManyRequestsException("Too many sign-in attempts; please wait a few minutes",
                    TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
        }
    }

    private static String normalise(String username) {
        return username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    }
}
