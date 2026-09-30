package com.softzenith.crm.identity;

/**
 * Which records a role can see. Phase 1 seeds every role with {@link #ALL}; the PRD's Phase 2
 * "counsellor sees own, branch manager sees branch" becomes a per-role setting, not new code.
 */
public enum DataScope {
    /** Every record in the tenant. */
    ALL,
    /** Records belonging to the user's branch. */
    BRANCH,
    /** Records assigned to the user. */
    OWN
}
