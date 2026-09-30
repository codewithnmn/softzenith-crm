package com.softzenith.crm.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Creates the default role set for a newly provisioned tenant. */
@Service
public class RoleProvisioner {

    private final RoleRepository roles;

    RoleProvisioner(RoleRepository roles) {
        this.roles = roles;
    }

    @Transactional
    public List<Role> createDefaultRoles(UUID tenantId) {
        return roles.saveAll(DefaultRoles.TEMPLATES.stream()
                .map(t -> Role.fromTemplate(tenantId, t))
                .toList());
    }
}
