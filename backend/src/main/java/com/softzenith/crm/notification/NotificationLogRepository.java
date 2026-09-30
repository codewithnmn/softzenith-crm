package com.softzenith.crm.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByLeadIdOrderByCreatedAt(UUID leadId);

    /** Already delivered for this event? (re-delivery after a crash or restart) */
    boolean existsByEventIdAndChannelAndKindAndRecipientAndStatusIn(UUID eventId, NotificationLog.Channel channel,
                                                                    NotificationLog.Kind kind, String recipient,
                                                                    Collection<NotificationLog.Status> statuses);

    long countByRecipientAndKindAndStatusInAndCreatedAtAfter(String recipient, NotificationLog.Kind kind,
                                                            Collection<NotificationLog.Status> statuses, Instant after);
}
