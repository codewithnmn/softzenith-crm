package com.softzenith.crm.identity;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Fills created_by / updated_by with the signed-in staff user's id (empty for system and public writes). */
@Component
class StaffAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return CurrentStaff.find().map(StaffPrincipal::userId);
    }
}
