package com.softzenith.crm.identity.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.softzenith.crm.shared.phone.PhoneNumbers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/** DEVELOPMENT ONLY: issues a Supabase-shaped token (subject + phone claim) for any phone number. */
@RestController
@RequestMapping("/api/v1/dev")
@ConditionalOnProperty("crm.auth.dev-login.enabled")
@Tag(name = "Dev login", description = "Development only: sign in by phone without OTP")
@SecurityRequirements
class DevLoginController {

    private final NimbusJwtEncoder encoder;

    DevLoginController(@Value("${crm.auth.dev-login.secret}") String secret) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(DevLogin.key(secret)));
    }

    record LoginRequest(@NotBlank String phone) {
    }

    record LoginResponse(String accessToken, Instant expiresAt) {
    }

    @PostMapping("/login")
    @Operation(summary = "Get an access token for a staff phone number (dev only)")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var phone = PhoneNumbers.toE164(request.phone(), "IN");
        var now = Instant.now();
        var expires = now.plus(12, ChronoUnit.HOURS);
        var claims = JwtClaimsSet.builder()
                .issuer(DevLogin.ISSUER)
                .subject("dev|" + phone)
                .audience(List.of("authenticated"))
                .claim("phone", phone.substring(1))
                .claim("amr", List.of(Map.of("method", "otp"))) // as Supabase does after a phone OTP
                .issuedAt(now)
                .expiresAt(expires)
                .build();
        var token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims));
        return new LoginResponse(token.getTokenValue(), expires);
    }
}
