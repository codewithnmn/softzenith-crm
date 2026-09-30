package com.softzenith.crm.identity;

import com.softzenith.crm.shared.persistence.TenantScopedEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** A tenant-defined role: a named bundle of permissions plus a record-visibility scope. */
@Entity
@Table(name = "roles")
public class Role extends TenantScopedEntity {

    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataScope dataScope;

    /** Users with this role can own records, i.e. appear in the "assign to" picker. */
    @Column(nullable = false)
    private boolean assignable;

    @Column(name = "is_system", nullable = false, updatable = false)
    private boolean system;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = EnumSet.noneOf(Permission.class);

    protected Role() {
    }

    public Role(UUID tenantId, String code, String name, String description, DataScope dataScope,
                boolean assignable, boolean system, Set<Permission> permissions) {
        super(tenantId);
        this.code = code;
        this.name = name;
        this.description = description;
        this.dataScope = dataScope;
        this.assignable = assignable;
        this.system = system;
        this.permissions = permissions.isEmpty() ? EnumSet.noneOf(Permission.class) : EnumSet.copyOf(permissions);
    }

    static Role fromTemplate(UUID tenantId, DefaultRoles.Template t) {
        return new Role(tenantId, t.code(), t.name(), t.description(), t.dataScope(), t.assignable(), true, t.permissions());
    }

    /** Tenant-editable settings of a role (its code, and whether it is a seeded system role, never change). */
    public void update(String name, String description, DataScope dataScope, boolean assignable) {
        this.name = name;
        this.description = description;
        this.dataScope = dataScope;
        this.assignable = assignable;
    }

    public boolean has(Permission permission) {
        return permissions.contains(permission);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DataScope getDataScope() { return dataScope; }
    public boolean isAssignable() { return assignable; }
    public boolean isSystem() { return system; }
    public Set<Permission> getPermissions() { return Collections.unmodifiableSet(permissions); }
}
