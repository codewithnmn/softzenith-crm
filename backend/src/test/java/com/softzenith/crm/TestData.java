package com.softzenith.crm;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/** Unique values so tests sharing one database never collide. */
public final class TestData {

    private TestData() {
    }

    public static String slug() {
        return "t-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * A token shaped like Supabase's after a phone OTP sign-in: subject = auth user id, phone without '+',
     * {@code amr} naming the OTP method (only then is the phone trusted for linking invited staff).
     */
    public static RequestPostProcessor otpToken(String subject, String phoneE164) {
        return jwt().jwt(j -> j.subject(subject).claim("phone", phoneE164.substring(1))
                .claim("amr", List.of(Map.of("method", "otp", "timestamp", 1))));
    }

    /** A valid, random Indian mobile number in E.164. */
    public static String phone() {
        return "+919" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
    }
}
