package com.softzenith.crm.identity.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/** When a token's phone claim may be trusted to link an invited staff row (only after an OTP). */
class JwtClaimsTest {

    @Test
    void aSupabaseOtpSessionProvesThePhone() {
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", "919000000001")
                .claim("amr", List.of(Map.of("method", "otp", "timestamp", 1)))))).isEqualTo("+919000000001");
    }

    @Test
    void plainStringAmrAndSmsCountToo() {
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", "+919000000001").claim("amr", List.of("sms")))))
                .isEqualTo("+919000000001");
    }

    @Test
    void aPasswordSessionDoesNotProveThePhone() {
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", "919000000001")
                .claim("amr", List.of(Map.of("method", "password")))))).isNull();
    }

    @Test
    void noAmrOrAMalformedAmrProvesNothing() {
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", "919000000001")))).isNull();
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", "919000000001").claim("amr", "otp")))).isNull();
    }

    @Test
    void aMissingBlankOrInvalidPhoneIsNull() {
        var otp = List.of(Map.of("method", "otp"));
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("amr", otp)))).isNull();
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", " ").claim("amr", otp)))).isNull();
        assertThat(JwtClaims.verifiedPhone(jwt(c -> c.claim("phone", "12").claim("amr", otp)))).isNull();
    }

    private static Jwt jwt(Consumer<Jwt.Builder> claims) {
        var builder = Jwt.withTokenValue("t").header("alg", "none").subject("s").issuedAt(Instant.now());
        claims.accept(builder);
        return builder.build();
    }
}
