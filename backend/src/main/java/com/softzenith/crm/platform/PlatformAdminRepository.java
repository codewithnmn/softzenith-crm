package com.softzenith.crm.platform;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, UUID> {

    Optional<PlatformAdmin> findByUsername(String username);
}
