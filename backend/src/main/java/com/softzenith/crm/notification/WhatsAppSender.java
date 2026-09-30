package com.softzenith.crm.notification;

/**
 * WhatsApp delivery, swappable per deployment ({@code crm.notifications.whatsapp.provider}).
 * Only a demo implementation exists until a WhatsApp Business API account (Meta Cloud API or an
 * aggregator such as Gupshup/Interakt) is available; adding one is a new implementation of this interface.
 */
public interface WhatsAppSender {

    record Result(NotificationLog.Status status, String provider, String providerRef) {
    }

    /** @param toE164 recipient in E.164 */
    Result send(String toE164, String text) throws Exception;
}
