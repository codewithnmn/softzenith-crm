package com.softzenith.crm.identity;

import java.util.UUID;

/** Read-only view of a staff user for other modules (assignment, notifications, display names). */
public record StaffSummary(UUID id, String fullName, String phoneE164, String email, String roleCode, String roleName,
                           boolean assignable, UserStatus status, UUID branchId, String branchName) {

    static StaffSummary of(AppUser u) {
        var branch = u.getBranch();
        return new StaffSummary(u.getId(), u.getFullName(), u.getPhoneE164(), u.getEmail(),
                u.getRole().getCode(), u.getRole().getName(), u.getRole().isAssignable(), u.getStatus(),
                branch == null ? null : branch.getId(), branch == null ? null : branch.getName());
    }

    public boolean enabled() {
        return status != UserStatus.DISABLED;
    }
}
