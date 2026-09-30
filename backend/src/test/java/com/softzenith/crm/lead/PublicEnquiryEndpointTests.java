package com.softzenith.crm.lead;

import com.softzenith.crm.Fixtures;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.tenancy.TenantSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Functional tests for the unauthenticated enquiry form (/api/v1/public/tenants/{slug}/...). Abuse limits are in
 * {@link PublicIntakeSecurityTests}; the captcha path is in {@link PublicEnquiryCaptchaTests}.
 */
@IntegrationTest
class PublicEnquiryEndpointTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;
    @Autowired JdbcTemplate jdbc;

    @Test
    void theFormShowsTheTenantsOptionsAndOnlyActiveBranches() throws Exception {
        var tenant = fixtures.tenant(new TenantSettings(null,
                new TenantSettings.EnquiryForm(List.of("Study Abroad", "Visitor Visa"), List.of("Canada", "UK")), null, null));
        var admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        fixtures.branch(tenant, "Rohtak");
        var closed = fixtures.branch(tenant, "Closed Office");
        mvc.perform(put("/api/v1/branches/{id}", closed.getId()).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Closed Office\",\"active\":false}")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/public/tenants/{slug}/enquiry-form", tenant.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branches[*].name", contains("Rohtak")))
                .andExpect(jsonPath("$.serviceInterests", contains("Study Abroad", "Visitor Visa")))
                .andExpect(jsonPath("$.countries", contains("Canada", "UK")));
    }

    @Test
    void aFilledHoneypotLooksLikeSuccessButStoresNothing() throws Exception {
        var tenant = fixtures.tenant();
        var admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        var phone = TestData.phone();
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Bot\",\"phone\":\"%s\",\"website\":\"http://spam.example\"}".formatted(phone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("will get in touch")));
        mvc.perform(get("/api/v1/leads").param("q", phone).with(admin.token()))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void unknownMalformedAndSuspendedTenantsAre404() throws Exception {
        var body = "{\"fullName\":\"Someone\",\"phone\":\"%s\"}".formatted(TestData.phone());
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", "no-such-tenant").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/public/tenants/{slug}/enquiry-form", "Bad_Slug!")).andExpect(status().isNotFound());

        var suspended = fixtures.tenant();
        jdbc.update("update tenants set status = 'SUSPENDED' where id = ?", suspended.getId());
        mvc.perform(get("/api/v1/public/tenants/{slug}/enquiry-form", suspended.getSlug())).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", suspended.getSlug()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesTheForm() throws Exception {
        var tenant = fixtures.tenant();
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"phone\":\"\",\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("email")))
                .andExpect(jsonPath("$.detail", containsString("fullName")))
                .andExpect(jsonPath("$.detail", containsString("phone")));
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Asha\",\"phone\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void utmAndSourceDetailAreKeptOnTheLead() throws Exception {
        var tenant = fixtures.tenant();
        var admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        var phone = TestData.phone();
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Campaign Lead","phone":"%s","sourceDetail":"Canada landing page",
                                 "utmSource":"facebook","utmMedium":"cpc","utmCampaign":"sep-intake"}""".formatted(phone)))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/leads").param("q", phone).with(admin.token()))
                .andExpect(jsonPath("$.items[0].sourceDetail").value("Canada landing page"))
                .andExpect(jsonPath("$.items[0].utmSource").value("facebook"))
                .andExpect(jsonPath("$.items[0].utmMedium").value("cpc"))
                .andExpect(jsonPath("$.items[0].utmCampaign").value("sep-intake"));
    }
}
