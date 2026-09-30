package com.softzenith.crm.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface LeadActivityRepository extends JpaRepository<LeadActivity, UUID> {

    List<LeadActivity> findByLeadIdOrderByCreatedAt(UUID leadId);
}
