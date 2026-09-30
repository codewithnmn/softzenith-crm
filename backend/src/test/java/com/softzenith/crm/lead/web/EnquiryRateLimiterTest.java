package com.softzenith.crm.lead.web;

import com.softzenith.crm.shared.web.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnquiryRateLimiterTest {

    private final UUID tenant = UUID.randomUUID();

    @Test
    void limitsEachClientIpSeparately() {
        var limiter = new EnquiryRateLimiter(2, 100, 100, 100);
        limiter.checkClient("10.0.0.1");
        limiter.checkClient("10.0.0.1");

        assertThatThrownBy(() -> limiter.checkClient("10.0.0.1"))
                .isInstanceOf(TooManyRequestsException.class)
                .satisfies(e -> assertThat(((TooManyRequestsException) e).retryAfterSeconds()).isBetween(1L, 61L));
        limiter.checkClient("10.0.0.2");
    }

    @Test
    void limitsEachPhoneWithinATenant() {
        var limiter = new EnquiryRateLimiter(100, 100, 2, 100);
        limiter.checkEnquiry(tenant, "+911");
        limiter.checkEnquiry(tenant, "+911");

        assertThatThrownBy(() -> limiter.checkEnquiry(tenant, "+911")).isInstanceOf(TooManyRequestsException.class);
        limiter.checkEnquiry(tenant, "+912");
        limiter.checkEnquiry(UUID.randomUUID(), "+911");
    }

    @Test
    void limitsEachTenantOverall() {
        var limiter = new EnquiryRateLimiter(100, 100, 100, 2);
        limiter.checkEnquiry(tenant, "+911");
        limiter.checkEnquiry(tenant, "+912");

        assertThatThrownBy(() -> limiter.checkEnquiry(tenant, "+913")).isInstanceOf(TooManyRequestsException.class);
        limiter.checkEnquiry(UUID.randomUUID(), "+913");
    }
}
