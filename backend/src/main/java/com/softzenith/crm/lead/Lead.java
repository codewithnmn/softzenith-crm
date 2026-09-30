package com.softzenith.crm.lead;

import com.softzenith.crm.shared.persistence.JsonMapConverter;
import com.softzenith.crm.shared.persistence.TenantScopedEntity;
import com.softzenith.crm.shared.text.Text;
import com.softzenith.crm.shared.web.ConflictException;
import com.softzenith.crm.shared.web.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** A raw enquiry, before it becomes a student/contact. Owns its status rules. */
@Entity
@Table(name = "leads")
public class Lead extends TenantScopedEntity {

    @Column(nullable = false, updatable = false)
    private String leadNumber;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneRaw;

    @Column(name = "phone_e164", nullable = false)
    private String phoneE164;

    /** Stored lower-case. */
    private String email;

    private String serviceInterest;
    private String preferredCountry;
    private String message;
    private UUID branchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private LeadSourceType sourceType;

    private String sourceDetail;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String externalRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status = LeadStatus.NEW;

    private UUID assignedTo;
    private Instant assignedAt;
    private UUID assignedBy;
    private String closedReason;
    private Instant closedAt;

    /** When this person last enquired; repeat enquiries bump it (see {@link LeadRepository#recordRepeatEnquiry}). */
    @Column(nullable = false)
    private Instant lastEnquiryAt = Instant.now();

    /** 1 for the first enquiry, +1 for each repeat enquiry while the lead is open. */
    @Column(nullable = false)
    private int enquiryCount = 1;

    @Convert(converter = JsonMapConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> customFields = new LinkedHashMap<>();

    protected Lead() {
    }

    Lead(String leadNumber, NewLead d, String phoneE164) {
        this.leadNumber = leadNumber;
        this.fullName = name(d.fullName());
        this.phoneRaw = Text.singleLine(d.phone());
        this.phoneE164 = phoneE164;
        this.email = normaliseEmail(d.email());
        this.serviceInterest = blankToNull(d.serviceInterest());
        this.preferredCountry = blankToNull(d.preferredCountry());
        this.message = Text.multiLine(d.message());
        this.branchId = d.branchId();
        this.sourceType = d.sourceType();
        this.sourceDetail = blankToNull(d.sourceDetail());
        this.utmSource = blankToNull(d.utmSource());
        this.utmMedium = blankToNull(d.utmMedium());
        this.utmCampaign = blankToNull(d.utmCampaign());
        this.externalRef = blankToNull(d.externalRef());
        if (d.customFields() != null) {
            this.customFields = new LinkedHashMap<>(d.customFields());
        }
    }

    void updateContact(String fullName, String phoneRaw, String phoneE164, String email, String serviceInterest,
                       String preferredCountry, UUID branchId, Map<String, Object> customFields) {
        this.fullName = name(fullName);
        this.phoneRaw = Text.singleLine(phoneRaw);
        this.phoneE164 = phoneE164;
        this.email = normaliseEmail(email);
        this.serviceInterest = blankToNull(serviceInterest);
        this.preferredCountry = blankToNull(preferredCountry);
        this.branchId = branchId;
        if (customFields != null) {
            this.customFields = new LinkedHashMap<>(customFields);
        }
    }

    void assignTo(UUID assignee, UUID actor) {
        if (status == LeadStatus.CLOSED) {
            throw new ConflictException("A closed lead cannot be assigned; reopen it first");
        }
        this.assignedTo = assignee;
        this.assignedBy = actor;
        this.assignedAt = Instant.now();
        this.status = LeadStatus.ASSIGNED;
    }

    /**
     * Moves to NEW, CONTACTED or CLOSED. ASSIGNED is only reached through {@link #assignTo}.
     * Leaving CLOSED is a reopen; the caller checks the reopen permission. A reopened lead goes back to the unassigned
     * queue for reception to reassign (owner, 30 Sep 2026).
     */
    void changeStatus(LeadStatus target, String reason) {
        if (target == status) {
            throw new ConflictException("Lead is already " + status);
        }
        switch (target) {
            case ASSIGNED -> throw new ConflictException("Assign the lead to a user to mark it ASSIGNED");
            case CLOSED -> {
                if (reason == null || reason.isBlank()) {
                    throw new InvalidInputException("A reason is required to close a lead");
                }
                this.closedReason = Text.singleLine(reason);
                this.closedAt = Instant.now();
            }
            case NEW, CONTACTED -> {
                if (status == LeadStatus.CLOSED) {
                    this.assignedTo = null;
                    this.assignedBy = null;
                    this.assignedAt = null;
                }
                this.closedReason = null;
                this.closedAt = null;
            }
        }
        this.status = target;
    }

    static String normaliseEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    private static String blankToNull(String s) {
        return Text.singleLine(s);
    }

    private static String name(String s) {
        var name = Text.singleLine(s);
        if (name == null) {
            throw new InvalidInputException("A name is required");
        }
        return name;
    }

    public String getLeadNumber() { return leadNumber; }
    public String getFullName() { return fullName; }
    public String getPhoneRaw() { return phoneRaw; }
    public String getPhoneE164() { return phoneE164; }
    public String getEmail() { return email; }
    public String getServiceInterest() { return serviceInterest; }
    public String getPreferredCountry() { return preferredCountry; }
    public String getMessage() { return message; }
    public UUID getBranchId() { return branchId; }
    public LeadSourceType getSourceType() { return sourceType; }
    public String getSourceDetail() { return sourceDetail; }
    public String getUtmSource() { return utmSource; }
    public String getUtmMedium() { return utmMedium; }
    public String getUtmCampaign() { return utmCampaign; }
    public String getExternalRef() { return externalRef; }
    public LeadStatus getStatus() { return status; }
    public UUID getAssignedTo() { return assignedTo; }
    public Instant getAssignedAt() { return assignedAt; }
    public UUID getAssignedBy() { return assignedBy; }
    public String getClosedReason() { return closedReason; }
    public Instant getClosedAt() { return closedAt; }
    public Instant getLastEnquiryAt() { return lastEnquiryAt; }
    public int getEnquiryCount() { return enquiryCount; }
    public Map<String, Object> getCustomFields() { return customFields; }
}
