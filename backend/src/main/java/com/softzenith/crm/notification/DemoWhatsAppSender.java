package com.softzenith.crm.notification;

import com.softzenith.crm.shared.logging.Mask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Sends nothing: the message is recorded in the lead's notification history as DEMO. */
@Component
@ConditionalOnProperty(name = "crm.notifications.whatsapp.provider", havingValue = "demo", matchIfMissing = true)
class DemoWhatsAppSender implements WhatsAppSender {

    private static final Logger log = LoggerFactory.getLogger(DemoWhatsAppSender.class);

    @Override
    public Result send(String toE164, String text) {
        // Nothing is sent. The full text is in notification_log; the log line stays free of personal data.
        log.debug("[DEMO WhatsApp] to {}: {} chars", Mask.phone(toE164), text.length());
        return new Result(NotificationLog.Status.DEMO, "demo-whatsapp", "demo-" + UUID.randomUUID());
    }
}
