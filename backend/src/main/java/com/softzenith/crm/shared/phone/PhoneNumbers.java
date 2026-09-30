package com.softzenith.crm.shared.phone;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.softzenith.crm.shared.web.InvalidInputException;

/**
 * Normalises user-typed phone numbers to E.164 ("+919876543210") so the same person is recognised
 * across sources and formats. Phone is the primary identity for staff logins and students.
 */
public final class PhoneNumbers {

    private static final PhoneNumberUtil UTIL = PhoneNumberUtil.getInstance();

    private PhoneNumbers() {
    }

    /**
     * @param raw           number as typed, with or without country code
     * @param defaultRegion ISO 3166 region used when {@code raw} has no country code (tenant default, e.g. "IN")
     */
    public static String toE164(String raw, String defaultRegion) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidInputException("Phone number is required");
        }
        try {
            var parsed = UTIL.parse(raw, defaultRegion);
            if (!UTIL.isValidNumber(parsed)) {
                throw new InvalidInputException("Invalid phone number: " + raw);
            }
            return UTIL.format(parsed, PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new InvalidInputException("Invalid phone number: " + raw);
        }
    }
}
