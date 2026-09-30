package com.softzenith.crm.lead;

/** Phase 1 statuses from the PRD. */
public enum LeadStatus {
    NEW,
    CONTACTED,
    /** Has an owner (counsellor). Reached through assignment, not a plain status change. */
    ASSIGNED,
    CLOSED
}
