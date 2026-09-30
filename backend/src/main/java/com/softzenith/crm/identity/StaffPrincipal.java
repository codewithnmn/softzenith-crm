package com.softzenith.crm.identity;

import java.util.Set;
import java.util.UUID;

/**
 * The signed-in staff user within one tenant: who they are, where they work and what they may do.
 * Built per request from the database, so role/permission changes apply immediately.
 */
public record StaffPrincipal(
        UUID userId,
        UUID tenantId,
        String tenantSlug,
        String tenantName,
        String fullName,
        String phoneE164,
        String email,
        String roleCode,
        String roleName,
        DataScope dataScope,
        Set<Permission> permissions,
        UUID branchId,
        String branchName) {

    public boolean has(Permission permission) {
        return permissions.contains(permission);
    }
}
