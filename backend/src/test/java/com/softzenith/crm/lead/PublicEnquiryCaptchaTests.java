package com.softzenith.crm.lead;

import com.softzenith.crm.Fixtures;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The enquiry form with captcha switched on (its own application context). Only the "no token" path is exercised:
 * a token would be sent to Cloudflare, and tests never call a live provider (the provider call is covered with a mock
 * server in {@code CaptchaVerifierTest}).
 */
@IntegrationTest
@TestPropertySource(properties = {"crm.public-intake.captcha.turnstile-secret=test-secret",
        "crm.public-intake.rate-limit.per-phone-per-hour=2"})
class PublicEnquiryCaptchaTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    @Test
    void anEnquiryWithoutACaptchaTokenIsRefused() throws Exception {
        var tenant = fixtures.tenant();
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"No Token\",\"phone\":\"%s\"}".formatted(TestData.phone())))
                .andExpect(status().isForbidden());
    }

    @Test
    void refusedRequestsDoNotUseUpTheVictimsPhoneAllowance() throws Exception {
        // Regression (REV1): the per-phone and per-tenant limits used to be charged before the captcha check, so a bot
        // without a captcha could lock a real enquirer out. Many unverified attempts must all be 403, never 429.
        var tenant = fixtures.tenant();
        var victim = TestData.phone();
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fullName\":\"Bot\",\"phone\":\"%s\"}".formatted(victim)))
                    .andExpect(status().isForbidden());
        }
    }
}
