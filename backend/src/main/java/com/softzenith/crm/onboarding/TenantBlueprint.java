package com.softzenith.crm.onboarding;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Everything needed to bring a new business onto the platform, as entered in the platform onboarding form (or saved
 * from it as a file). Every field may be missing while the form is being filled in; {@link BlueprintValidator} reports
 * what is still needed.
 */
public record TenantBlueprint(OnboardingBusiness business, OnboardingSettings settings,
                              List<OnboardingBranch> branches, List<OnboardingStaff> staff) {

    /**
     * @param slug          the tenant's address in URLs, e.g. {@code westernworld} → /enquiry/westernworld
     * @param defaultRegion ISO 3166 region used for phone numbers typed without a country code, e.g. IN
     * @param timezone      IANA zone, e.g. Asia/Kolkata
     */
    public record OnboardingBusiness(String name, String slug, String defaultRegion, String timezone) {
    }

    /** See {@code tenancy.TenantSettings}; null means the platform default. */
    public record OnboardingSettings(String leadNumberPrefix, List<String> serviceInterests, List<String> countries,
                                     String senderName, Boolean welcomeEmail, Boolean welcomeWhatsApp,
                                     Boolean studentUpdates, Set<String> features) {
    }

    public record OnboardingBranch(String name, String city) {
    }

    /**
     * @param role   a default role, by code or name: Admin, Branch Manager, Counsellor, Receptionist
     * @param branch the name of one of the branches above, or empty
     */
    public record OnboardingStaff(String fullName, String phone, String email, String role, String branch,
                                  String employeeCode, String designation, LocalDate joinedOn) {
    }
}
