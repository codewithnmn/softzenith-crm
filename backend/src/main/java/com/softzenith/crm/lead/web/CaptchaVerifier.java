package com.softzenith.crm.lead.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Cloudflare Turnstile check for the public enquiry form. Off when no secret is configured (local development);
 * when on, an enquiry without a valid token is refused. The verification call is the provider boundary (mock it).
 */
@Component
class CaptchaVerifier {

    static final String SITEVERIFY = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private static final Logger log = LoggerFactory.getLogger(CaptchaVerifier.class);

    private final String secret;
    private final RestClient http;

    @Autowired
    CaptchaVerifier(@Value("${crm.public-intake.captcha.turnstile-secret:}") String secret) {
        this(secret, RestClient.builder());
    }

    CaptchaVerifier(String secret, RestClient.Builder http) {
        this.secret = secret == null ? "" : secret.strip();
        this.http = http.build();
    }

    boolean enabled() {
        return !secret.isEmpty();
    }

    /** True when captcha is off, or the provider confirms the token. Provider errors count as "not verified". */
    boolean verify(String token, String clientIp) {
        if (!enabled()) {
            return true;
        }
        if (token == null || token.isBlank()) {
            return false;
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("secret", secret);
        form.add("response", token);
        form.add("remoteip", clientIp);
        try {
            var body = http.post().uri(SITEVERIFY).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                    .retrieve().body(Map.class);
            return body != null && Boolean.TRUE.equals(body.get("success"));
        } catch (RuntimeException e) {
            log.warn("Captcha verification failed: {}", e.toString());
            return false;
        }
    }
}
