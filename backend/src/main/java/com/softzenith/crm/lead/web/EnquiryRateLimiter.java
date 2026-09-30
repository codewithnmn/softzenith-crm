package com.softzenith.crm.lead.web;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.softzenith.crm.shared.web.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token-bucket limits on the public enquiry endpoint: per client IP, per phone number within a tenant (stops one
 * person's phone / inbox being bombarded through the form), and per tenant overall. In memory, so each instance
 * counts separately; move the buckets to a shared store (Bucket4j JDBC / Redis) when running several instances.
 */
@Component
class EnquiryRateLimiter {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(200_000)
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    private final List<Bandwidth> perIp;
    private final List<Bandwidth> perPhone;
    private final List<Bandwidth> perTenant;

    EnquiryRateLimiter(@Value("${crm.public-intake.rate-limit.per-ip-per-minute}") int ipPerMinute,
                       @Value("${crm.public-intake.rate-limit.per-ip-per-hour}") int ipPerHour,
                       @Value("${crm.public-intake.rate-limit.per-phone-per-hour}") int phonePerHour,
                       @Value("${crm.public-intake.rate-limit.per-tenant-per-hour}") int tenantPerHour) {
        this.perIp = List.of(limit(ipPerMinute, Duration.ofMinutes(1)), limit(ipPerHour, Duration.ofHours(1)));
        this.perPhone = List.of(limit(phonePerHour, Duration.ofHours(1)));
        this.perTenant = List.of(limit(tenantPerHour, Duration.ofHours(1)));
    }

    /** Before the captcha check: a flood from one address only drains its own bucket and can't hammer the captcha API. */
    void checkClient(String clientIp) {
        take("ip:" + clientIp, perIp, "Too many enquiries from your network; please try again later");
    }

    /**
     * After the captcha check, so unverified requests can't drain a victim's phone bucket or the tenant-wide bucket
     * and lock real enquirers out.
     *
     * @param phoneKey the normalised phone (E.164)
     */
    void checkEnquiry(UUID tenantId, String phoneKey) {
        take("phone:" + tenantId + ":" + phoneKey, perPhone, "Too many enquiries for this phone number; please try again later");
        take("tenant:" + tenantId, perTenant, "We are receiving too many enquiries right now; please try again later");
    }

    private void take(String key, List<Bandwidth> limits, String message) {
        var bucket = buckets.get(key, k -> {
            var builder = Bucket.builder();
            limits.forEach(builder::addLimit);
            return builder.build();
        });
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            throw new TooManyRequestsException(message, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
        }
    }

    private static Bandwidth limit(int capacity, Duration period) {
        return Bandwidth.builder().capacity(capacity).refillGreedy(capacity, period).build();
    }
}
