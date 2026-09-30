package com.softzenith.crm.platform.web;

import com.softzenith.crm.onboarding.OnboardingPlan;
import com.softzenith.crm.onboarding.TenantBlueprint;
import com.softzenith.crm.onboarding.TenantOnboardingService;
import com.softzenith.crm.onboarding.TenantOnboardingService.Onboarded;
import com.softzenith.crm.platform.PlatformSessions;
import com.softzenith.crm.shared.logging.LogContext;
import com.softzenith.crm.tenancy.Tenant;
import com.softzenith.crm.tenancy.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SoftZenith platform administration: sign-in, the tenant list, and onboarding a new business (preview, then go live).
 * Separate from every tenant's staff API; see {@code PlatformSecurityConfig}.
 */
@RestController
@RequestMapping("/api/platform")
@Tag(name = "Platform", description = "SoftZenith platform administration (not tenant staff)")
class PlatformController {

    private final PlatformSessions sessions;
    private final TenantOnboardingService onboarding;
    private final TenantRepository tenants;

    PlatformController(PlatformSessions sessions, TenantOnboardingService onboarding, TenantRepository tenants) {
        this.sessions = sessions;
        this.onboarding = onboarding;
        this.tenants = tenants;
    }

    record PlatformLoginRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 200) String password) {
    }

    record PlatformSession(String username, String accessToken, Instant expiresAt) {
    }

    record PlatformMe(String username) {
    }

    record PlatformTenant(UUID id, String slug, String name, Tenant.Status status, Instant createdAt) {
    }

    /** One answer for every refusal, so the form reveals nothing about which usernames exist. */
    static final class SignInRefused extends RuntimeException {
        SignInRefused() {
            super("Wrong username or password, or the account is locked for a while after repeated failures");
        }
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Sign in as a platform admin; returns a bearer token for the other /api/platform endpoints")
    PlatformSession login(@Valid @RequestBody PlatformLoginRequest r, HttpServletRequest request) {
        return sessions.login(r.username(), r.password(), request.getRemoteAddr())
                .map(s -> new PlatformSession(s.username(), s.accessToken(), s.expiresAt()))
                .orElseThrow(SignInRefused::new);
    }

    @ExceptionHandler(SignInRefused.class)
    ProblemDetail signInRefused(SignInRefused e) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problem.setProperty("requestId", LogContext.requestId());
        return problem;
    }

    @GetMapping("/me")
    PlatformMe me(@AuthenticationPrincipal Jwt admin) {
        return new PlatformMe(admin.getSubject());
    }

    @GetMapping("/tenants")
    @Operation(summary = "Every business on the platform, newest first")
    List<PlatformTenant> tenants() {
        return tenants.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(t -> new PlatformTenant(t.getId(), t.getSlug(), t.getName(), t.getStatus(), t.getCreatedAt()))
                .toList();
    }

    @PostMapping("/tenants/preview")
    @Operation(summary = "Check a new business's setup and show what going live would create (changes nothing)")
    OnboardingPlan preview(@RequestBody TenantBlueprint blueprint) {
        return onboarding.preview(blueprint);
    }

    @PostMapping("/tenants")
    @Operation(summary = "Go live: create the business, its settings, roles, branches and invited staff in one step")
    ResponseEntity<Onboarded> goLive(@RequestBody TenantBlueprint blueprint, @AuthenticationPrincipal Jwt admin) {
        var result = onboarding.goLive(blueprint);
        sessions.recordOnboarded(admin.getSubject(), result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
