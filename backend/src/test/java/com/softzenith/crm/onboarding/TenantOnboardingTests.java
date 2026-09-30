package com.softzenith.crm.onboarding;

import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.identity.AppUser;
import com.softzenith.crm.identity.AppUserRepository;
import com.softzenith.crm.identity.Branch;
import com.softzenith.crm.identity.BranchRepository;
import com.softzenith.crm.identity.DataScope;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.identity.Permission;
import com.softzenith.crm.identity.Role;
import com.softzenith.crm.identity.RoleRepository;
import com.softzenith.crm.shared.tenant.TenantContext;
import com.softzenith.crm.shared.web.ConflictException;
import com.softzenith.crm.tenancy.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static com.softzenith.crm.TestData.phone;
import static com.softzenith.crm.TestData.slug;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Onboarding, per-tenant uniqueness, and tenant isolation enforced by Hibernate and database constraints. */
@IntegrationTest
class TenantOnboardingTests {

    @Autowired TenantOnboardingService onboarding;
    @Autowired RoleRepository roles;
    @Autowired BranchRepository branches;
    @Autowired AppUserRepository users;

    @Test
    void onboardingCreatesTheFourDefaultRoles() {
        var tenant = onboard();

        var created = TenantContext.call(tenant.getId(), roles::findAllByOrderByName);
        assertThat(created).extracting(Role::getCode)
                .containsExactlyInAnyOrder(DefaultRoles.ADMIN, DefaultRoles.BRANCH_MANAGER, DefaultRoles.COUNSELLOR, DefaultRoles.RECEPTIONIST);
        assertThat(created).allMatch(Role::isSystem);
        // Owner, 2026-09-26 (V7): counsellors see their own leads, branch managers their branch, the rest everything.
        assertThat(created).extracting(Role::getCode, Role::getDataScope).containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(DefaultRoles.ADMIN, DataScope.ALL),
                org.assertj.core.groups.Tuple.tuple(DefaultRoles.BRANCH_MANAGER, DataScope.BRANCH),
                org.assertj.core.groups.Tuple.tuple(DefaultRoles.COUNSELLOR, DataScope.OWN),
                org.assertj.core.groups.Tuple.tuple(DefaultRoles.RECEPTIONIST, DataScope.ALL));

        assertThat(role(tenant, DefaultRoles.ADMIN).getPermissions()).containsExactlyInAnyOrder(Permission.values());
        var counsellor = role(tenant, DefaultRoles.COUNSELLOR);
        assertThat(counsellor.isAssignable()).isTrue();
        assertThat(counsellor.has(Permission.LEAD_VIEW)).isTrue();
        assertThat(counsellor.has(Permission.LEAD_ASSIGN)).isFalse();
        assertThat(counsellor.has(Permission.LEAD_CHANGE_STATUS)).isTrue(); // owner, 2026-09-26 (V5)
        assertThat(counsellor.has(Permission.LEAD_REOPEN)).isFalse();
    }

    @Test
    void duplicateSlugIsRejected() {
        var slug = slug();
        onboarding.onboard(slug, "One", "IN", "Asia/Kolkata");
        assertThatThrownBy(() -> onboarding.onboard(slug, "Two", "IN", "Asia/Kolkata"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rowsCreatedInATenantContextAreStampedWithThatTenant() {
        var tenant = onboard();
        var branch = TenantContext.call(tenant.getId(), () -> branches.saveAndFlush(new Branch("HQ", "Delhi")));
        assertThat(branch.getTenantId()).isEqualTo(tenant.getId());
    }

    @Test
    void queriesOnlySeeTheCurrentTenantsRows() {
        var a = onboard();
        var b = onboard();
        var branchOfA = TenantContext.call(a.getId(), () -> branches.saveAndFlush(new Branch("A HQ", null)));
        var branchOfB = TenantContext.call(b.getId(), () -> branches.saveAndFlush(new Branch("B HQ", null)));

        TenantContext.run(a.getId(), () -> {
            assertThat(branches.findAll()).extracting(Branch::getId).containsExactly(branchOfA.getId());
            assertThat(branches.findById(branchOfB.getId())).isEmpty();
            assertThat(roles.findAll()).allMatch(r -> r.getTenantId().equals(a.getId())).hasSize(4);
        });
    }

    @Test
    void withoutATenantContextNothingIsVisibleAndNothingCanBeWritten() {
        var tenant = onboard();
        TenantContext.run(tenant.getId(), () -> branches.saveAndFlush(new Branch("HQ", null)));

        assertThat(branches.findAll()).isEmpty();
        assertThatThrownBy(() -> branches.saveAndFlush(new Branch("Orphan", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void phoneIsUniqueWithinATenantButNotAcrossTenants() {
        var a = onboard();
        var b = onboard();
        var phone = phone();

        TenantContext.run(a.getId(), () -> users.saveAndFlush(user(phone, role(a, DefaultRoles.COUNSELLOR))));
        TenantContext.run(b.getId(), () -> users.saveAndFlush(user(phone, role(b, DefaultRoles.COUNSELLOR))));

        assertThatThrownBy(() -> TenantContext.run(a.getId(),
                () -> users.saveAndFlush(user(phone, role(a, DefaultRoles.ADMIN)))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aUserCannotReferenceAnotherTenantsRoleOrBranch() {
        var a = onboard();
        var b = onboard();
        var branchOfB = TenantContext.call(b.getId(), () -> branches.saveAndFlush(new Branch("B HQ", null)));
        var roleOfA = role(a, DefaultRoles.ADMIN);
        var roleOfB = role(b, DefaultRoles.ADMIN);

        TenantContext.run(a.getId(), () -> {
            assertThatThrownBy(() -> users.saveAndFlush(user(phone(), roleOfB)))
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThatThrownBy(() -> users.saveAndFlush(new AppUser("Y", phone(), null, roleOfA, branchOfB)))
                    .isInstanceOf(DataIntegrityViolationException.class);
        });
    }

    private Tenant onboard() {
        return onboarding.onboard(slug(), "Acme Visas", "IN", "Asia/Kolkata");
    }

    private Role role(Tenant tenant, String code) {
        return TenantContext.call(tenant.getId(), () -> roles.findByCode(code).orElseThrow());
    }

    private static AppUser user(String phone, Role role) {
        return new AppUser("Test User", phone, null, role, null);
    }
}
