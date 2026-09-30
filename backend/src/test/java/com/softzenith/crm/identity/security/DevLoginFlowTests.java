package com.softzenith.crm.identity.security;

import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.identity.DefaultRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The dev sign-in end to end with real signed tokens (its own context, dev login on): phone → token → /me. Unlike the
 * other tests, nothing here fakes the JWT, so this is also the test for token verification itself.
 */
@IntegrationTest
@TestPropertySource(properties = {"crm.auth.dev-login.enabled=true",
        "crm.auth.dev-login.secret=test-secret-at-least-32-characters-long"})
class DevLoginFlowTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    @Test
    void aStaffPhoneSignsInAndTheTokenOpensTheirWorkspace() throws Exception {
        var tenant = fixtures.tenant();
        var receptionist = fixtures.staff(tenant, DefaultRoles.RECEPTIONIST);

        var login = mvc.perform(post("/api/v1/dev/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + receptionist.phone().substring(3) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(receptionist.id().toString()))
                .andExpect(jsonPath("$.role.code").value(DefaultRoles.RECEPTIONIST));
    }

    @Test
    void aPhoneThatIsNotStaffGetsATokenButNoWorkspace() throws Exception {
        var login = mvc.perform(post("/api/v1/dev/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9811111111\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void forgedMalformedOrMissingTokensAre401() throws Exception {
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
        // Signed with another key: header.payload.signature that does not verify.
        mvc.perform(get("/api/v1/me").header("Authorization",
                        "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwiaXNzIjoiY3JtLWRldi1sb2dpbiJ9.c2lnbmF0dXJl"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void aStaleTokenOnAPublicPageIsIgnored() throws Exception {
        var tenant = fixtures.tenant();
        mvc.perform(get("/api/v1/public/tenants/{slug}/enquiry-form", tenant.getSlug())
                        .header("Authorization", "Bearer expired-or-garbage"))
                .andExpect(status().isOk());
    }

    @Test
    void theLoginNeedsAValidPhone() throws Exception {
        mvc.perform(post("/api/v1/dev/login").contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/dev/login").contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\"12\"}"))
                .andExpect(status().isBadRequest());
    }
}
