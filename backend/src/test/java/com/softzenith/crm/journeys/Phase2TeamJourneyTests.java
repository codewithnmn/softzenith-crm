package com.softzenith.crm.journeys;

import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRD Phase 2, "Counsellors + branches", as one story: an admin sets up branches and a team, each person signs in with
 * their phone, reception routes enquiries to counsellors, and every role sees exactly the leads its data scope allows.
 *
 * <p>Read top to bottom; each step names the rule it proves. The per-endpoint tests ({@code StaffEndpointTests},
 * {@code LeadEndpointTests}, {@code LeadScopeTests}) narrow down a failing step.
 */
@IntegrationTest
class Phase2TeamJourneyTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    @Test
    void anAdminBuildsATeamAndEachRoleWorksOnlyItsOwnLeads() throws Exception {
        var tenant = fixtures.tenant();
        var admin = fixtures.staff(tenant, DefaultRoles.ADMIN);

        // 1. Admin sets up two branches.
        var rohini = id(mvc.perform(post("/api/v1/branches").with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Rohini\",\"city\":\"Delhi\"}")).andExpect(status().isCreated()));
        var rohtak = id(mvc.perform(post("/api/v1/branches").with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Rohtak\",\"city\":\"Rohtak\"}")).andExpect(status().isCreated()));

        // 2. Admin invites a branch manager and a counsellor for Rohini and a receptionist; all start INVITED.
        var managerPhone = TestData.phone();
        var counsellorPhone = TestData.phone();
        var receptionPhone = TestData.phone();
        var manager = invite(admin, "Naman Manager", managerPhone, DefaultRoles.BRANCH_MANAGER, tenant, rohini);
        var counsellor = invite(admin, "Indu Counsellor", counsellorPhone, DefaultRoles.COUNSELLOR, tenant, rohini);
        invite(admin, "Front Desk", receptionPhone, DefaultRoles.RECEPTIONIST, tenant, null);

        // 3. Each signs in with their phone (OTP) for the first time: the invitation is claimed and they become ACTIVE.
        var asManager = signIn("sub-manager", managerPhone);
        var asCounsellor = signIn("sub-counsellor", counsellorPhone);
        var asReception = signIn("sub-reception", receptionPhone);
        mvc.perform(get("/api/v1/me").with(asCounsellor)).andExpect(status().isOk())
                .andExpect(jsonPath("$.role.code").value(DefaultRoles.COUNSELLOR))
                .andExpect(jsonPath("$.branch.name").value("Rohini"));
        mvc.perform(get("/api/v1/users/{id}", counsellor).with(admin.token())).andExpect(jsonPath("$.status").value("ACTIVE"));

        // 4. Enquiries arrive for both branches.
        var rohiniLead = walkIn(asReception, "Aman Gupta", rohini);
        var rohtakLead = walkIn(asReception, "Kavya Rao", rohtak);

        // 5. Reception sees both in the unassigned queue, and the counsellor (OWN scope) sees neither yet.
        mvc.perform(get("/api/v1/leads").param("unassigned", "true").with(asReception))
                .andExpect(jsonPath("$.items[*].id", containsInAnyOrder(rohiniLead, rohtakLead)));
        mvc.perform(get("/api/v1/leads").with(asCounsellor)).andExpect(jsonPath("$.totalElements").value(0));

        // 6. The branch manager (BRANCH scope) sees Rohini's lead but not Rohtak's.
        mvc.perform(get("/api/v1/leads").with(asManager))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(rohiniLead));
        mvc.perform(get("/api/v1/leads/{id}", rohtakLead).with(asManager)).andExpect(status().isNotFound());

        // 7. Reception picks the counsellor from the assignable list and assigns the Rohini lead.
        mvc.perform(get("/api/v1/users").param("assignable", "true").with(asReception))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(manager, counsellor)));
        mvc.perform(put("/api/v1/leads/{id}/assignment", rohiniLead).with(asReception).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + counsellor + "\"}"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTo.name").value("Indu Counsellor"));

        // 8. The counsellor now sees it, works it (CONTACTED), but cannot assign or open the dashboard.
        mvc.perform(get("/api/v1/leads").with(asCounsellor))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(rohiniLead));
        changeStatus(asCounsellor, rohiniLead, "{\"status\":\"CONTACTED\"}").andExpect(status().isOk());
        mvc.perform(put("/api/v1/leads/{id}/assignment", rohtakLead).with(asCounsellor).contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + counsellor + "\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/leads/stats").with(asCounsellor)).andExpect(status().isForbidden());

        // 9. Admin's dashboard shows the whole picture.
        mvc.perform(get("/api/v1/leads/stats").with(admin.token()))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.byStatus.CONTACTED").value(1))
                .andExpect(jsonPath("$.openUnassigned").value(1))
                .andExpect(jsonPath("$.byAssignee[0].name").value("Indu Counsellor"));

        // 10. The counsellor closes the lead; only admin can reopen it, and it goes back to reception's queue.
        changeStatus(asCounsellor, rohiniLead, "{\"status\":\"CLOSED\",\"reason\":\"Budget\"}").andExpect(status().isOk());
        changeStatus(asCounsellor, rohiniLead, "{\"status\":\"NEW\"}").andExpect(status().isForbidden());
        changeStatus(admin.token(), rohiniLead, "{\"status\":\"NEW\"}").andExpect(jsonPath("$.assignedTo").doesNotExist());
        mvc.perform(get("/api/v1/leads").param("unassigned", "true").with(asReception))
                .andExpect(jsonPath("$.items[*].id", containsInAnyOrder(rohiniLead, rohtakLead)));

        // 11. The counsellor leaves: admin disables them; they can no longer sign in or be assigned leads.
        mvc.perform(post("/api/v1/users/{id}/disable", counsellor).with(admin.token())).andExpect(jsonPath("$.status").value("DISABLED"));
        mvc.perform(get("/api/v1/me").with(asCounsellor)).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/leads/{id}/assignment", rohiniLead).with(asReception).contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + counsellor + "\"}")).andExpect(status().isConflict());
    }

    private String invite(Fixtures.Staff admin, String name, String phone, String role,
                          com.softzenith.crm.tenancy.Tenant tenant, String branchId) throws Exception {
        var body = """
                {"fullName":"%s","phone":"%s","email":"%s@acme.test","roleId":"%s"%s}""".formatted(name, phone,
                phone.substring(1), fixtures.roleId(tenant, role), branchId == null ? "" : ",\"branchId\":\"" + branchId + "\"");
        return id(mvc.perform(post("/api/v1/users").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INVITED")));
    }

    private static RequestPostProcessor signIn(String subject, String phone) {
        return TestData.otpToken(subject, phone);
    }

    private String walkIn(RequestPostProcessor as, String name, String branchId) throws Exception {
        return id(mvc.perform(post("/api/v1/leads").with(as).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fullName":"%s","phone":"%s","sourceType":"WALK_IN","branchId":"%s"}""".formatted(name, TestData.phone(), branchId)))
                .andExpect(status().isCreated()));
    }

    private ResultActions changeStatus(RequestPostProcessor as, String leadId, String body) throws Exception {
        return mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(as).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static String id(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
