package com.softzenith.crm.notification;

import com.softzenith.crm.shared.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/** One notification attempt, whatever the outcome. */
@Entity
@Table(name = "notification_log")
public class NotificationLog extends TenantScopedEntity {

    public enum Channel { EMAIL, WHATSAPP }

    public enum Kind {
        LEAD_WELCOME, NEW_LEAD_ALERT, LEAD_ASSIGNED,
        /** To staff: an enquirer with an open lead got in touch again. */
        REPEAT_ENQUIRY_ALERT,
        /** To the enquirer: acknowledgement of a repeat enquiry. */
        REPEAT_ENQUIRY_ACK,
        /** To the enquirer: their enquiry was assigned or changed status. */
        LEAD_STATUS_UPDATE
    }

    /** DEMO = accepted by a demo provider; nothing left the system. SKIPPED = not sent on purpose (daily cap). */
    public enum Status { SENT, FAILED, DEMO, SKIPPED }

    private UUID leadId;

    /** The lead event this attempt was made for, so a re-delivered event does not send again. */
    @Column(updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Kind kind;

    @Column(nullable = false, updatable = false)
    private String recipient;

    @Column(updatable = false)
    private String subject;

    @Column(nullable = false, updatable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private String provider;

    private String providerRef;
    private String error;

    protected NotificationLog() {
    }

    NotificationLog(UUID eventId, UUID leadId, Channel channel, Kind kind, String recipient, String subject, String body,
                    Status status, String provider, String providerRef, String error) {
        this.eventId = eventId;
        this.leadId = leadId;
        this.channel = channel;
        this.kind = kind;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.provider = provider;
        this.providerRef = providerRef;
        this.error = error;
    }

    public UUID getLeadId() { return leadId; }
    public Channel getChannel() { return channel; }
    public Kind getKind() { return kind; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Status getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getProviderRef() { return providerRef; }
    public String getError() { return error; }
}
