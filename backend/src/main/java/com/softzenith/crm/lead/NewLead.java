package com.softzenith.crm.lead;

import java.util.Map;
import java.util.UUID;

/** Normalised input every lead source adapter (website form, staff entry, later Meta/WhatsApp) produces. */
public record NewLead(String fullName, String phone, String email, String serviceInterest, String preferredCountry,
                      String message, UUID branchId, LeadSourceType sourceType, String sourceDetail,
                      String utmSource, String utmMedium, String utmCampaign, String externalRef,
                      Map<String, Object> customFields) {

    /**
     * Allowed characters in a person's name: letters (any script), combining marks, spaces and . ' -.
     * Keeps URLs, digits and markup out of the name, which is quoted in messages to the enquirer and to staff.
     */
    public static final String NAME_PATTERN = "[\\p{L}\\p{M}][\\p{L}\\p{M} .'\u2019-]*";
    public static final String NAME_MESSAGE = "may only contain letters, spaces and . ' -";
}
