package com.softzenith.crm.lead;

import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.Fixtures.Staff;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DataScope;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.identity.Permission;
import com.softzenith.crm.tenancy.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A role's data scope (ALL / BRANCH / OWN) decides which leads its holders can see and touch. */
@IntegrationTest
class LeadScopeTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    Tenant tenant;
    Staff admin;
    Staff own;        // OWN scope: only leads assigned to them
    Staff delhiLead;  // BRANCH scope, Delhi branch
    String inDelhi;
    String inPune;
    String puneAssignedToOwn;

    @BeforeEach
    void setUp() throws Exception {
        tenant = fixtures.tenant();
        var delhi = fixtures.branch(tenant, "Delhi");
        var pune = fixtures.branch(tenant, "Pune");
        fixtures.role(tenant, "OWN_COUNSELLOR", DataScope.OWN, true,
                EnumSet.of(Permission.LEAD_VIEW, Permission.LEAD_EDIT, Permission.LEAD_CHANGE_STATUS));
        fixtures.role(tenant, "BRANCH_LEAD", DataScope.BRANCH, false, EnumSet.of(Permission.LEAD_VIEW));
        admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        own = fixtures.staff(tenant, "OWN_COUNSELLOR");
        delhiLead = fixtures.staff(tenant, "BRANCH_LEAD", delhi);

        inDelhi = create(delhi.getId());
        inPune = create(pune.getId());
        puneAssignedToOwn = create(pune.getId());
        mvc.perform(put("/api/v1/leads/{id}/assignment", puneAssignedToOwn).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + own.id() + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void ownScopeSeesOnlyAssignedLeads() throws Exception {
        mvc.perform(get("/api/v1/leads").with(own.token()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(puneAssignedToOwn));
        mvc.perform(get("/api/v1/leads/{id}", puneAssignedToOwn).with(own.token())).andExpect(status().isOk());

        mvc.perform(get("/api/v1/leads/{id}", inDelhi).with(own.token())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/leads/{id}/activities", inDelhi).with(own.token())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/leads/{id}/notifications", inDelhi).with(own.token())).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/leads/{id}/status", inDelhi).with(own.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONTACTED\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void branchScopeSeesItsBranchAndAllScopeSeesEverything() throws Exception {
        mvc.perform(get("/api/v1/leads").with(delhiLead.token()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(inDelhi));
        mvc.perform(get("/api/v1/leads/{id}", inPune).with(delhiLead.token())).andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/leads").with(admin.token()))
                .andExpect(jsonPath("$.items[*].id", containsInAnyOrder(inDelhi, inPune, puneAssignedToOwn)));
    }

    @Test
    void branchScopeOpensLeadsOfItsBranchAndLeadsAssignedToThem() throws Exception {
        mvc.perform(get("/api/v1/leads/{id}", inDelhi).with(delhiLead.token())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/leads/{id}/activities", inDelhi).with(delhiLead.token())).andExpect(status().isOk());
    }

    @Test
    void branchScopeWithoutABranchFallsBackToOwnLeads() throws Exception {
        fixtures.role(tenant, "BRANCH_ASSIGNABLE", DataScope.BRANCH, true, EnumSet.of(Permission.LEAD_VIEW));
        var floating = fixtures.staff(tenant, "BRANCH_ASSIGNABLE"); // no branch set
        mvc.perform(get("/api/v1/leads").with(floating.token())).andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(put("/api/v1/leads/{id}/assignment", inPune).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + floating.id() + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/leads").with(floating.token()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(inPune));
        mvc.perform(get("/api/v1/leads/{id}", inPune).with(floating.token())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/leads/{id}", inDelhi).with(floating.token())).andExpect(status().isNotFound());
    }

    private String create(UUID branchId) throws Exception {
        var body = """
                {"fullName":"Walk In","phone":"%s","sourceType":"WALK_IN","branchId":"%s"}""".formatted(TestData.phone(), branchId);
        var result = mvc.perform(post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }
}
