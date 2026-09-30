package com.softzenith.crm.identity;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Access to the signed-in staff user for services in any module. */
public final class CurrentStaff {

    private CurrentStaff() {
    }

    public static Optional<StaffPrincipal> find() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof StaffPrincipal staff ? Optional.of(staff) : Optional.empty();
    }

    public static StaffPrincipal require() {
        return find().orElseThrow(() -> new IllegalStateException("No signed-in staff user"));
    }
}
