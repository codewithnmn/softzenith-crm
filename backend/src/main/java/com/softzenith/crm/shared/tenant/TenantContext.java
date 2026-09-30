package com.softzenith.crm.shared.tenant;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The tenant the current thread works for. Hibernate reads it when a session opens and filters every
 * tenant-scoped query by it (see {@code TenantScopedEntity}), so it must be set <em>before</em> a
 * transaction starts.
 *
 * <ul>
 *   <li>Staff requests: set by the security filter from the signed-in user's membership.</li>
 *   <li>Public intake / jobs: set explicitly via {@link #call(UUID, Supplier)}.</li>
 *   <li>Cross-tenant platform work (onboarding, sign-in lookup): {@link #callAsSystem(Supplier)}.</li>
 * </ul>
 * With nothing set, queries match no tenant and inserts fail. Forgetting the context fails closed.
 */
public final class TenantContext {

    /** Hibernate "root" tenant: tenant filter disabled, explicit tenant ids allowed on insert. */
    public static final UUID SYSTEM = new UUID(0, 0);

    /** Used when no context is set; matches no real tenant. */
    static final UUID NONE = new UUID(0, 1);

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    /** The current real tenant, empty in system context or when unset. */
    public static Optional<UUID> current() {
        return Optional.ofNullable(CURRENT.get()).filter(id -> !SYSTEM.equals(id));
    }

    public static UUID require() {
        return current().orElseThrow(() -> new IllegalStateException("No tenant in context"));
    }

    public static <T> T call(UUID tenantId, Supplier<T> action) {
        var previous = CURRENT.get();
        CURRENT.set(tenantId);
        try {
            return action.get();
        } finally {
            restore(previous);
        }
    }

    public static void run(UUID tenantId, Runnable action) {
        call(tenantId, () -> {
            action.run();
            return null;
        });
    }

    public static <T> T callAsSystem(Supplier<T> action) {
        return call(SYSTEM, action);
    }

    /** For request filters that wrap the whole filter chain; always pair with {@link #clear()}. */
    public static void set(UUID tenantId) {
        CURRENT.set(tenantId);
    }

    public static void clear() {
        CURRENT.remove();
    }

    static UUID resolve() {
        var id = CURRENT.get();
        return id == null ? NONE : id;
    }

    private static void restore(UUID previous) {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }
}
