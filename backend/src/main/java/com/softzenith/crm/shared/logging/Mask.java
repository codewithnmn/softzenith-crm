package com.softzenith.crm.shared.logging;

/**
 * Shortens personal data before it is logged: enough to recognise a record, not enough to contact the person.
 * Logs are read by more people and kept longer than the database, so phones and emails never go in whole.
 */
public final class Mask {

    private Mask() {
    }

    /** +919876543210 → +91******3210 */
    public static String phone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        var p = phone.trim();
        if (p.length() <= 6) {
            return "*".repeat(p.length());
        }
        return p.substring(0, 3) + "*".repeat(p.length() - 7) + p.substring(p.length() - 4);
    }

    /** asha.verma@mail.com → a***@mail.com */
    public static String email(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        var at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /** Either kind of recipient (notification log recipients are a phone or an email). */
    public static String recipient(String value) {
        return value != null && value.contains("@") ? email(value) : phone(value);
    }
}
