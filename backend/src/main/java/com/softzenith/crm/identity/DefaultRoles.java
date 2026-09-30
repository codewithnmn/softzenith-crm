package com.softzenith.crm.identity;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.softzenith.crm.identity.Permission.LEAD_ASSIGN;
import static com.softzenith.crm.identity.Permission.LEAD_CHANGE_STATUS;
import static com.softzenith.crm.identity.Permission.LEAD_CREATE;
import static com.softzenith.crm.identity.Permission.LEAD_EDIT;
import static com.softzenith.crm.identity.Permission.LEAD_NEW_ALERT;
import static com.softzenith.crm.identity.Permission.LEAD_VIEW;
import static com.softzenith.crm.identity.Permission.USER_VIEW;

/**
 * Roles every new tenant starts with. Tenants can rename them, change their permissions and scope,
 * or add their own roles; these are only the starting point.
 * Rules: Admin and Receptionist see every lead, a Branch Manager sees their branch, a Counsellor only the leads assigned
 * to them (owner, 26 Sep 2026; V7); Admin and Receptionist assign leads to counsellors; Admin and Counsellor change
 * status (V5); only Admin reopens and sees the lead dashboard. Admin and Receptionist get new-lead alerts.
 */
public final class DefaultRoles {

    public static final String ADMIN = "ADMIN";
    public static final String BRANCH_MANAGER = "BRANCH_MANAGER";
    public static final String COUNSELLOR = "COUNSELLOR";
    public static final String RECEPTIONIST = "RECEPTIONIST";

    public record Template(String code, String name, String description, DataScope dataScope,
                           boolean assignable, Set<Permission> permissions) {
    }

    private static final Set<Permission> STAFF = EnumSet.of(LEAD_VIEW, LEAD_CREATE, LEAD_EDIT, USER_VIEW);
    private static final Set<Permission> COUNSELLING = EnumSet.of(LEAD_VIEW, LEAD_CREATE, LEAD_EDIT, USER_VIEW, LEAD_CHANGE_STATUS);
    private static final Set<Permission> RECEPTION = EnumSet.of(LEAD_VIEW, LEAD_CREATE, LEAD_EDIT, USER_VIEW, LEAD_NEW_ALERT, LEAD_ASSIGN);

    public static final List<Template> TEMPLATES = List.of(
            new Template(ADMIN, "Admin", "Full access to the tenant, including users, roles and settings",
                    DataScope.ALL, false, EnumSet.allOf(Permission.class)),
            new Template(BRANCH_MANAGER, "Branch Manager", "Manages a branch and its team",
                    DataScope.BRANCH, true, STAFF),
            new Template(COUNSELLOR, "Counsellor", "Works assigned leads and students",
                    DataScope.OWN, true, COUNSELLING),
            new Template(RECEPTIONIST, "Receptionist", "Captures walk-in and phone enquiries",
                    DataScope.ALL, false, RECEPTION));

    private DefaultRoles() {
    }
}
