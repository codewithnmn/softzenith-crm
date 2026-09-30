package com.softzenith.crm.notification.web;

import com.softzenith.crm.lead.LeadService;
import com.softzenith.crm.notification.NotificationLog;
import com.softzenith.crm.notification.NotificationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Notifications", description = "Delivery history")
class NotificationController {

    private final NotificationLogRepository logs;
    private final LeadService leads;

    NotificationController(NotificationLogRepository logs, LeadService leads) {
        this.logs = logs;
        this.leads = leads;
    }

    @GetMapping("/api/v1/leads/{leadId}/notifications")
    @PreAuthorize("hasAuthority('LEAD_VIEW')")
    @Operation(summary = "Every email/WhatsApp sent (or attempted) about this lead")
    List<NotificationResponse> forLead(@PathVariable UUID leadId) {
        leads.get(leadId); // 404 unless the lead is within the caller's data scope
        return logs.findByLeadIdOrderByCreatedAt(leadId).stream().map(NotificationResponse::of).toList();
    }

    record NotificationResponse(UUID id, NotificationLog.Channel channel, NotificationLog.Kind kind, String recipient,
                                String subject, String body, NotificationLog.Status status, String provider,
                                String error, Instant at) {
        static NotificationResponse of(NotificationLog n) {
            return new NotificationResponse(n.getId(), n.getChannel(), n.getKind(), n.getRecipient(), n.getSubject(),
                    n.getBody(), n.getStatus(), n.getProvider(), n.getError(), n.getCreatedAt());
        }
    }
}
