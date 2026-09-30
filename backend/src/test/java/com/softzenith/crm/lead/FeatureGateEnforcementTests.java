package com.softzenith.crm.lead;

import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.Fixtures.Staff;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.shared.tenant.FeatureGate;
import com.softzenith.crm.tenancy.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A tenant's plan (subscription feature gates), orthogonal to permissions: a tenant whose plan is missing
 * {@link FeatureGate#TEAM_AND_STUDENTS} gets 403 on team features even for its Admin, but keeps bare lead capture
 * ({@link FeatureGate#LEADS_CORE}).
 */
@IntegrationTest
class FeatureGateEnforcementTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    @Test
    void teamFeaturesAre403WithoutThePlanGate() throws Exception {
        Tenant tenant = fixtures.tenant(Set.of(FeatureGate.LEADS_CORE));
        Staff admin = fixtures.staff(tenant, DefaultRoles.ADMIN);

        String leadId = createLead(admin);

        mvc.perform(get("/api/v1/leads/stats").with(admin.token()))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + admin.id() + "\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONTACTED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/users").with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"New Hire","phone":"%s","roleId":"%s"}"""
                                .formatted(TestData.phone(), fixtures.roleId(tenant, DefaultRoles.COUNSELLOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    void leadsCoreStaysAvailableWithoutTeamAndStudents() throws Exception {
        Tenant tenant = fixtures.tenant(Set.of(FeatureGate.LEADS_CORE));
        Staff admin = fixtures.staff(tenant, DefaultRoles.ADMIN);

        createLead(admin);
        mvc.perform(get("/api/v1/leads").with(admin.token())).andExpect(status().isOk());
    }

    @Test
    void leadIntakeIs403WithoutLeadsCore() throws Exception {
        Tenant tenant = fixtures.tenant(Set.of(FeatureGate.TEAM_AND_STUDENTS));
        Staff admin = fixtures.staff(tenant, DefaultRoles.ADMIN);

        mvc.perform(post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Walk In","phone":"%s","sourceType":"WALK_IN"}""".formatted(TestData.phone())))
                .andExpect(status().isForbidden());
    }

    private String createLead(Staff admin) throws Exception {
        var body = """
                {"fullName":"Walk In","phone":"%s","sourceType":"WALK_IN"}""".formatted(TestData.phone());
        var result = mvc.perform(post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }
}
