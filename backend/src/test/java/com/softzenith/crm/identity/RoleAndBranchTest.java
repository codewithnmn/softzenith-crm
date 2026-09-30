package com.softzenith.crm.identity;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleAndBranchTest {

    @Test
    void aRoleWithNoPermissionsHasNone() {
        var role = new Role(UUID.randomUUID(), "VIEWER", "Viewer", null, DataScope.OWN, false, false, Set.of());
        assertThat(role.getPermissions()).isEmpty();
        assertThat(role.has(Permission.LEAD_VIEW)).isFalse();
    }

    @Test
    void aRolesPermissionsCannotBeChangedFromOutside() {
        var role = new Role(UUID.randomUUID(), "X", "X", null, DataScope.ALL, false, false, EnumSet.of(Permission.LEAD_VIEW));
        assertThatThrownBy(() -> role.getPermissions().add(Permission.USER_MANAGE))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void aTenantCanRenameARoleAndChangeItsScopeButNotItsCode() {
        var role = new Role(UUID.randomUUID(), "COUNSELLOR", "Counsellor", null, DataScope.OWN, true, true,
                EnumSet.of(Permission.LEAD_VIEW));
        role.update("Visa Advisor", "Handles visa leads", DataScope.BRANCH, false);

        assertThat(role.getCode()).isEqualTo("COUNSELLOR");
        assertThat(role.getName()).isEqualTo("Visa Advisor");
        assertThat(role.getDescription()).isEqualTo("Handles visa leads");
        assertThat(role.getDataScope()).isEqualTo(DataScope.BRANCH);
        assertThat(role.isAssignable()).isFalse();
        assertThat(role.isSystem()).isTrue();
    }

    @Test
    void aBranchStartsActiveAndCanBeDeactivated() {
        var branch = new Branch("Rohtak", "Rohtak");
        assertThat(branch.isActive()).isTrue();
        branch.update("Rohtak HQ", null, false);
        assertThat(branch.getName()).isEqualTo("Rohtak HQ");
        assertThat(branch.getCity()).isNull();
        assertThat(branch.isActive()).isFalse();
    }
}
