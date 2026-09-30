package com.softzenith.crm.lead;

import com.softzenith.crm.shared.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/** Append-only history entry of a lead. {@code createdBy} is the acting staff user (null for public intake). */
@Entity
@Table(name = "lead_activities")
public class LeadActivity extends TenantScopedEntity {

    /** ASSIGNED and UNASSIGNED carry user ids in from/to. */
    public enum Type { CREATED, REPEAT_ENQUIRY, UPDATED, STATUS_CHANGED, ASSIGNED, UNASSIGNED }

    @Column(nullable = false, updatable = false)
    private UUID leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Type type;

    @Column(updatable = false)
    private String fromValue;

    @Column(updatable = false)
    private String toValue;

    @Column(updatable = false)
    private String note;

    protected LeadActivity() {
    }

    LeadActivity(UUID leadId, Type type, String fromValue, String toValue, String note) {
        this.leadId = leadId;
        this.type = type;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.note = note;
    }

    public UUID getLeadId() { return leadId; }
    public Type getType() { return type; }
    public String getFromValue() { return fromValue; }
    public String getToValue() { return toValue; }
    public String getNote() { return note; }
}
