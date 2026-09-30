package com.softzenith.crm.identity;

import com.softzenith.crm.shared.tenant.TenantContext;
import com.softzenith.crm.tenancy.Tenant;
import com.softzenith.crm.tenancy.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves which tenants a signed-in identity is staff of.
 *
 * <p>Staff are invited by phone number. The first time someone signs in with a verified phone
 * (identity-provider OTP), every invited {@link AppUser} row with that phone is linked to their
 * identity-provider subject; from then on they are matched by subject.
 */
@Service
public class StaffDirectory {

    private static final Logger log = LoggerFactory.getLogger(StaffDirectory.class);

    private final AppUserRepository users;
    private final TenantRepository tenants;
    private final TransactionTemplate tx;

    StaffDirectory(AppUserRepository users, TenantRepository tenants, TransactionTemplate tx) {
        this.users = users;
        this.tenants = tenants;
        this.tx = tx;
    }

    /**
     * @param authSubject    identity-provider user id (JWT {@code sub})
     * @param verifiedPhone  E.164 phone verified by the identity provider, or null
     * @return one principal per tenant where the user is active staff and the tenant is active
     */
    public List<StaffPrincipal> memberships(String authSubject, String verifiedPhone) {
        // Sign-in is the one place that legitimately looks across tenants.
        return TenantContext.callAsSystem(() -> tx.execute(status -> {
            var rows = new ArrayList<>(users.findWithRoleByAuthSubject(authSubject));
            if (verifiedPhone != null) {
                // Also on later sign-ins: the person may since have been invited by another tenant (or re-invited
                // after a sign-in reset). Skip tenants where this identity already has a row.
                var linkedTenants = rows.stream().map(AppUser::getTenantId).collect(Collectors.toSet());
                var invited = users.findUnlinkedWithRoleByPhone(verifiedPhone).stream()
                        .filter(u -> !linkedTenants.contains(u.getTenantId()))
                        .toList();
                invited.forEach(u -> u.linkAuthSubject(authSubject));
                if (!invited.isEmpty()) {
                    log.info("Linked identity {} to {} staff membership(s)", authSubject, invited.size());
                }
                rows.addAll(invited);
            }
            var active = rows.stream().filter(AppUser::isActive).toList();
            Map<UUID, Tenant> tenantById = tenants.findAllById(active.stream().map(AppUser::getTenantId).toList())
                    .stream().collect(Collectors.toMap(Tenant::getId, Function.identity()));
            return active.stream()
                    .filter(u -> tenantById.get(u.getTenantId()).getStatus() == Tenant.Status.ACTIVE)
                    .map(u -> toPrincipal(u, tenantById.get(u.getTenantId())))
                    .toList();
        }));
    }

    private static StaffPrincipal toPrincipal(AppUser u, Tenant tenant) {
        var role = u.getRole();
        var branch = u.getBranch();
        return new StaffPrincipal(u.getId(), tenant.getId(), tenant.getSlug(), tenant.getName(),
                u.getFullName(), u.getPhoneE164(), u.getEmail(),
                role.getCode(), role.getName(), role.getDataScope(), role.getPermissions(),
                branch == null ? null : branch.getId(), branch == null ? null : branch.getName());
    }
}
