package com.softzenith.crm.lead;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lead counts for leads created in [from, to). Every status and source is present (0 when none), so the
 * dashboard layout never shifts.
 */
public record LeadStats(Instant from, Instant to, long total, long openUnassigned, Map<LeadStatus, Long> byStatus,
                        Map<LeadSourceType, Long> bySource, List<AssigneeCount> byAssignee) {

    public record AssigneeCount(UUID userId, String name, long count) {
    }
}
