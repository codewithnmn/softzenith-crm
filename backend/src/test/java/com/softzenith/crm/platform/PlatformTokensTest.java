package com.softzenith.crm.platform;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Platform session tokens on their own: issued, verified, and refused when signed with any other key. */
class PlatformTokensTest {

    private static final String SECRET = "a-platform-secret-of-at-least-32-chars";

    @Test
    void anIssuedTokenVerifiesAndNamesTheAdmin() {
        var tokens = new PlatformTokens(SECRET);
        var issued = tokens.issue("softzenith");
        var jwt = tokens.decoder().decode(issued.accessToken());
        assertThat(jwt.getSubject()).isEqualTo("softzenith");
        assertThat(jwt.getAudience()).containsExactly(PlatformTokens.ISSUER);
        assertThat(issued.expiresAt()).isEqualTo(jwt.getExpiresAt());
    }

    @Test
    void tokensFromAnotherKeyAreRefused() {
        var issued = new PlatformTokens(SECRET).issue("softzenith");
        assertThatThrownBy(() -> new PlatformTokens("another-secret-that-is-32-characters-x").decoder().decode(issued.accessToken()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void withoutASecretEachStartUsesItsOwnRandomKey() {
        var first = new PlatformTokens("");
        var restarted = new PlatformTokens(null);
        var issued = first.issue("softzenith");
        assertThat(first.decoder().decode(issued.accessToken()).getSubject()).isEqualTo("softzenith");
        assertThatThrownBy(() -> restarted.decoder().decode(issued.accessToken())).isInstanceOf(JwtException.class);
    }

    @Test
    void aShortSecretStopsStartUp() {
        assertThatThrownBy(() -> new PlatformTokens("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
