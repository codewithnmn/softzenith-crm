package com.softzenith.crm.platform;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Issues and checks platform-admin session tokens: HS256, our own key, issuer and audience {@value #ISSUER}. Staff
 * tokens (Supabase or dev login) are signed with other keys, so they can never pass here, and these never pass there.
 *
 * <p>The decoder is deliberately not a Spring bean: a {@code JwtDecoder} bean would replace the staff (Supabase) one.
 */
@Component
class PlatformTokens {

    static final String ISSUER = "crm-platform";
    static final Duration LIFETIME = Duration.ofHours(4);

    private static final Logger log = LoggerFactory.getLogger(PlatformTokens.class);

    record Issued(String accessToken, Instant expiresAt) {
    }

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;

    PlatformTokens(@Value("${crm.platform.jwt-secret:}") String secret) {
        var key = key(secret);
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(ISSUER),
                new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(ISSUER))));
    }

    Issued issue(String username) {
        // Whole seconds, as a JWT stores them, so the expiry we report is exactly the token's.
        var now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        var expires = now.plus(LIFETIME);
        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER).audience(List.of(ISSUER)).subject(username)
                .issuedAt(now).expiresAt(expires)
                .build();
        var token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims));
        return new Issued(token.getTokenValue(), expires);
    }

    JwtDecoder decoder() {
        return decoder;
    }

    private static SecretKey key(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("crm.platform.jwt-secret is not set: using a random key, so platform-admin sessions end on restart");
            var random = new byte[32];
            new SecureRandom().nextBytes(random);
            return new SecretKeySpec(random, "HmacSHA256");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("crm.platform.jwt-secret must be at least 32 characters");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
