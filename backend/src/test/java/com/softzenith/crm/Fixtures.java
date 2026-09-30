package com.softzenith.crm;

import com.softzenith.crm.identity.AppUser;
import com.softzenith.crm.identity.AppUserRepository;
import com.softzenith.crm.identity.Branch;
import com.softzenith.crm.identity.BranchRepository;
import com.softzenith.crm.identity.DataScope;
import com.softzenith.crm.identity.Permission;
import com.softzenith.crm.identity.Role;
import com.softzenith.crm.identity.RoleRepository;
import com.softzenith.crm.onboarding.TenantOnboardingService;
import com.softzenith.crm.shared.tenant.FeatureGate;
import com.softzenith.crm.shared.tenant.TenantContext;
import com.softzenith.crm.tenancy.Tenant;
import com.softzenith.crm.tenancy.TenantRepository;
import com.softzenith.crm.tenancy.TenantSettings;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.UUID;

/** Builds tenants and staff for integration tests. */
@Component
public class Fixtures {

    private final TenantOnboardingService onboarding;
    private final RoleRepository roles;
    private final AppUserRepository users;
    private final BranchRepository branches;
    private final TenantRepository tenants;
    private final TransactionTemplate tx;

    Fixtures(TenantOnboardingService onboarding, RoleRepository roles, AppUserRepository users,
             BranchRepository branches, TenantRepository tenants, TransactionTemplate tx) {
        this.onboarding = onboarding;
        this.roles = roles;
        this.users = users;
        this.branches = branches;
        this.tenants = tenants;
        this.tx = tx;
    }

    public record Staff(UUID id, String phone, String email) {
        /** A request signed in as this staff member (Supabase-shaped token). */
        public RequestPostProcessor token() {
            return TestData.otpToken("sub-" + phone, phone);
        }
    }

    public Tenant tenant() {
        return onboarding.onboard(TestData.slug(), "Acme Visas", "IN", "Asia/Kolkata");
    }

    /** A tenant whose plan only includes the given gates, e.g. {@code tenant(Set.of(FeatureGate.LEADS_CORE))}. */
    public Tenant tenant(Set<FeatureGate> features) {
        return tenant(new TenantSettings(null, null, null, features));
    }

    /** A tenant with the given settings, e.g. notifications switched off. */
    public Tenant tenant(TenantSettings settings) {
        var tenant = tenant();
        TenantContext.callAsSystem(() -> tx.execute(status -> {
            var t = tenants.findById(tenant.getId()).orElseThrow();
            t.updateSettings(settings);
            return tenants.save(t);
        }));
        return tenant;
    }

    /** A tenant-defined role, e.g. a narrower "HR" role or an OWN-scope counsellor role. Use its code with {@link #staff}. */
    public UUID role(Tenant tenant, String code, DataScope scope, boolean assignable, Set<Permission> permissions) {
        return TenantContext.call(tenant.getId(), () -> tx.execute(s -> roles.save(
                new Role(tenant.getId(), code, code, null, scope, assignable, false, permissions)).getId()));
    }

    public Staff staff(Tenant tenant, String roleCode) {
        return staff(tenant, roleCode, null);
    }

    public Staff staff(Tenant tenant, String roleCode, Branch branch) {
        return staff(tenant, roleCode, branch, roleCode.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8) + "@acme.test");
    }

    /** A staff member with no email address (they get no email alerts). */
    public Staff staffWithoutEmail(Tenant tenant, String roleCode) {
        return staff(tenant, roleCode, null, null);
    }

    private Staff staff(Tenant tenant, String roleCode, Branch branch, String email) {
        var phone = TestData.phone();
        var id = TenantContext.call(tenant.getId(), () -> tx.execute(s ->
                users.save(new AppUser("Test " + roleCode, phone, email, roles.findByCode(roleCode).orElseThrow(), branch)).getId()));
        return new Staff(id, phone, email);
    }

    public Branch branch(Tenant tenant, String name) {
        return TenantContext.call(tenant.getId(), () -> tx.execute(s -> branches.save(new Branch(name, null))));
    }

    public UUID roleId(Tenant tenant, String code) {
        return TenantContext.call(tenant.getId(), () -> roles.findByCode(code).orElseThrow().getId());
    }
}
