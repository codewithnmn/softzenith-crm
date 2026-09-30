package com.softzenith.crm.lead.web;

import com.softzenith.crm.identity.StaffLookup;
import com.softzenith.crm.lead.LeadService;
import com.softzenith.crm.lead.LeadSourceType;
import com.softzenith.crm.lead.NewLead;
import com.softzenith.crm.shared.phone.PhoneNumbers;
import com.softzenith.crm.shared.tenant.FeatureGate;
import com.softzenith.crm.shared.tenant.FeatureGateService;
import com.softzenith.crm.shared.tenant.TenantContext;
import com.softzenith.crm.shared.web.NotFoundException;
import com.softzenith.crm.tenancy.Tenant;
import com.softzenith.crm.tenancy.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.softzenith.crm.shared.logging.LogContext;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Website enquiry form for any tenant, addressed by tenant slug, so every onboarded business gets a
 * working form with no setup. Unauthenticated, so: honeypot, captcha (when configured), rate limits, and the same
 * answer whether or not the person already has an enquiry (the form must not reveal who is a client).
 */
@RestController
@RequestMapping("/api/v1/public/tenants/{slug}")
@Tag(name = "Public enquiry", description = "Unauthenticated website enquiry form")
@SecurityRequirements
class PublicEnquiryController {

    private static final Logger log = LoggerFactory.getLogger(PublicEnquiryController.class);

    private final TenantRepository tenants;
    private final StaffLookup staff;
    private final LeadService leads;
    private final EnquiryRateLimiter rateLimiter;
    private final CaptchaVerifier captcha;
    private final FeatureGateService featureGate;

    PublicEnquiryController(TenantRepository tenants, StaffLookup staff, LeadService leads,
                            EnquiryRateLimiter rateLimiter, CaptchaVerifier captcha, FeatureGateService featureGate) {
        this.tenants = tenants;
        this.staff = staff;
        this.leads = leads;
        this.rateLimiter = rateLimiter;
        this.captcha = captcha;
        this.featureGate = featureGate;
    }

    @GetMapping("/enquiry-form")
    @Operation(summary = "What the enquiry form should show for this tenant")
    FormResponse form(@PathVariable String slug) {
        var tenant = activeTenant(slug);
        var branches = TenantContext.call(tenant.getId(), staff::activeBranches).stream()
                .map(b -> new BranchOption(b.getId(), b.getName())).toList();
        var form = tenant.getSettings().enquiryForm();
        return new FormResponse(tenant.getName(), branches, form.serviceInterests(), form.countries());
    }

    @PostMapping("/enquiries")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit an enquiry; becomes a lead (or is added to the person's open lead)")
    EnquiryResponse submit(@PathVariable String slug, @Valid @RequestBody EnquiryRequest r, HttpServletRequest request) {
        var tenant = activeTenant(slug);
        MDC.put(LogContext.TENANT_ID, tenant.getId().toString()); // cleared by RequestLoggingFilter
        if (r.website() != null && !r.website().isBlank()) {
            // Honeypot field filled in: almost certainly a bot. Answer like a success, store nothing.
            log.info("Enquiry for tenant '{}' dropped: honeypot filled (likely a bot)", slug);
            return new EnquiryResponse(thanks(tenant));
        }
        var clientIp = request.getRemoteAddr();
        rateLimiter.checkClient(clientIp);
        if (!captcha.verify(r.captchaToken(), clientIp)) {
            log.info("Enquiry for tenant '{}' refused: captcha missing or invalid", slug);
            throw new AccessDeniedException("Please complete the verification and try again");
        }
        rateLimiter.checkEnquiry(tenant.getId(), PhoneNumbers.toE164(r.phone(), tenant.getDefaultRegion()));
        TenantContext.call(tenant.getId(), () -> {
            featureGate.requireFeature(FeatureGate.LEADS_CORE);
            return leads.intake(new NewLead(r.fullName(), r.phone(), r.email(),
                    r.serviceInterest(), r.preferredCountry(), r.message(), r.branchId(), LeadSourceType.WEBSITE_FORM,
                    r.sourceDetail(), r.utmSource(), r.utmMedium(), r.utmCampaign(), null, null));
        });
        // Same answer for a new and a repeat enquiry, and no lead number: the reference goes out by email instead.
        return new EnquiryResponse(thanks(tenant));
    }

    private Tenant activeTenant(String slug) {
        var tenant = SLUG.matcher(slug).matches() ? tenants.findBySlug(slug) : java.util.Optional.<Tenant>empty();
        if (tenant.isPresent() && tenant.get().getStatus() != Tenant.Status.ACTIVE) {
            log.warn("Enquiry or form request for inactive tenant '{}'", slug);
        }
        return tenant
                .filter(t -> t.getStatus() == Tenant.Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Tenant", slug));
    }

    private static String thanks(Tenant tenant) {
        return "Thank you! Someone from the " + tenant.getName() + " team will get in touch with you shortly.";
    }

    private static final java.util.regex.Pattern SLUG = java.util.regex.Pattern.compile("[a-z0-9-]{1,64}");

    record EnquiryRequest(@NotBlank @Size(max = 200) @Pattern(regexp = NewLead.NAME_PATTERN, message = NewLead.NAME_MESSAGE)
                          String fullName,
                          @NotBlank @Size(max = 30) String phone,
                          @Email @Size(max = 200) String email,
                          @Size(max = 200) String serviceInterest,
                          @Size(max = 100) String preferredCountry,
                          UUID branchId,
                          @Size(max = 2000) String message,
                          @Size(max = 200) String sourceDetail,
                          @Size(max = 200) String utmSource,
                          @Size(max = 200) String utmMedium,
                          @Size(max = 200) String utmCampaign,
                          /* honeypot: hidden on the form, humans leave it empty */ String website,
                          /* Cloudflare Turnstile token; required when the backend has a captcha secret */
                          @Size(max = 4096) String captchaToken) {
    }

    record BranchOption(UUID id, String name) {
    }

    record FormResponse(String tenantName, List<BranchOption> branches, List<String> serviceInterests,
                        List<String> countries) {
    }

    record EnquiryResponse(String message) {
    }
}
