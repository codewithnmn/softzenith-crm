package com.softzenith.crm.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The identity module's read API for other modules. Runs in the current tenant context. */
@Service
@Transactional(readOnly = true)
public class StaffLookup {

    private final AppUserRepository users;
    private final BranchRepository branches;

    StaffLookup(AppUserRepository users, BranchRepository branches) {
        this.users = users;
        this.branches = branches;
    }

    public Optional<StaffSummary> user(UUID id) {
        return users.findAllWithRoleAndBranch(List.of(id)).stream().findFirst().map(StaffSummary::of);
    }

    public Map<UUID, StaffSummary> users(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return users.findAllWithRoleAndBranch(ids).stream()
                .map(StaffSummary::of)
                .collect(Collectors.toMap(StaffSummary::id, Function.identity()));
    }

    /** Enabled staff whose role grants {@code permission}, e.g. everyone who receives new-lead alerts. */
    public List<StaffSummary> withPermission(Permission permission) {
        return users.findEnabledWithPermission(permission).stream().map(StaffSummary::of).toList();
    }

    public Optional<Branch> branch(UUID id) {
        return branches.findById(id);
    }

    public Map<UUID, String> branchNames(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return branches.findAllById(ids).stream().collect(Collectors.toMap(Branch::getId, Branch::getName));
    }

    public List<Branch> activeBranches() {
        return branches.findAllByOrderByName().stream().filter(Branch::isActive).toList();
    }
}
