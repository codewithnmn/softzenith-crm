package com.softzenith.crm.identity.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevLoginTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void refusesToStartWithTheProdProfile() {
        var env = new MockEnvironment();
        env.setActiveProfiles("dev", "prod");
        assertThatThrownBy(() -> new DevLogin().devJwtDecoder(SECRET, env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prod");
    }

    @Test
    void refusesAShortSecret() {
        var env = new MockEnvironment();
        env.setActiveProfiles("dev");
        assertThatThrownBy(() -> new DevLogin().devJwtDecoder("too-short", env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void startsWithoutIt() {
        var env = new MockEnvironment();
        env.setActiveProfiles("dev");
        assertThat(new DevLogin().devJwtDecoder(SECRET, env)).isNotNull();
    }
}
