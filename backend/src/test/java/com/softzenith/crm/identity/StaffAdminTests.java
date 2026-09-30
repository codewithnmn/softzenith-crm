package com.softzenith.crm.identity;

import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.Fixtures.Staff;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.tenancy.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Admin onboards staff and branches; other roles cannot. */
@IntegrationTest
class StaffAdminTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    Tenant tenant;
    Staff admin;
    Staff receptionist;

    @BeforeEach
    void setUp() {
        tenant = fixtures.tenant();
        admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        receptionist = fixtures.staff(tenant, DefaultRoles.RECEPTIONIST);
    }

    @Test
    void adminOnboardsACounsellorWhoCanThenSignIn() throws Exception {
        var branch = fixtures.branch(tenant, "Pune");
        var phone = TestData.phone();
        var body = """
                {"fullName":"Neha Counsellor","phone":"%s","email":"neha@acme.test","roleId":"%s","branchId":"%s",
                 "employeeCode":"EMP-7","designation":"Senior Counsellor","joinedOn":"2026-09-01"}"""
                .formatted(phone.substring(3), fixtures.roleId(tenant, DefaultRoles.COUNSELLOR), branch.getId());

        var created = mvc.perform(post("/api/v1/users").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INVITED"))
                .andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.role.code").value(DefaultRoles.COUNSELLOR))
                .andExpect(jsonPath("$.branch.name").value("Pune"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-7"))
                .andReturn();
        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(get("/api/v1/users").param("assignable", "true").with(receptionist.token()))
                .andExpect(jsonPath("$[*].id", hasItem(id)))
                .andExpect(jsonPath("$[*].id", not(hasItem(receptionist.id().toString()))));

        mvc.perform(get("/api/v1/me").with(TestData.otpToken("neha", phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role.code").value(DefaultRoles.COUNSELLOR))
                .andExpect(jsonPath("$.branch.name").value("Pune"));
        mvc.perform(get("/api/v1/users/{id}", id).with(admin.token()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(post("/api/v1/users/{id}/disable", id).with(admin.token()))
                .andExpect(jsonPath("$.status").value("DISABLED"));
        mvc.perform(get("/api/v1/me").with(TestData.otpToken("neha", phone)))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicatePhoneIsRejected() throws Exception {
        var body = """
                {"fullName":"Dup","phone":"%s","roleId":"%s"}""".formatted(receptionist.phone(), fixtures.roleId(tenant, DefaultRoles.COUNSELLOR));
        mvc.perform(post("/api/v1/users").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void nonAdminsCannotManageStaffOrBranches() throws Exception {
        var body = """
                {"fullName":"X","phone":"%s","roleId":"%s"}""".formatted(TestData.phone(), fixtures.roleId(tenant, DefaultRoles.ADMIN));
        mvc.perform(post("/api/v1/users").with(receptionist.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/branches").with(receptionist.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Rogue\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesBranchesAndEveryoneCanListThem() throws Exception {
        mvc.perform(post("/api/v1/branches").with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Chandigarh\",\"city\":\"Chandigarh\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/branches").with(receptionist.token()))
                .andExpect(jsonPath("$[*].name", hasItem("Chandigarh")));
        mvc.perform(get("/api/v1/roles").with(admin.token()))
                .andExpect(jsonPath("$[*].code", hasItem(DefaultRoles.COUNSELLOR)));
    }

    @Test
    void aNarrowerUserManagerCannotGrantOrManageStrongerRoles() throws Exception {
        var hrRole = fixtures.role(tenant, "HR", DataScope.ALL, false,
                EnumSet.of(Permission.USER_VIEW, Permission.USER_MANAGE, Permission.LEAD_VIEW));
        var hr = fixtures.staff(tenant, "HR");

        mvc.perform(post("/api/v1/users").with(hr.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(staffBody(TestData.phone(), fixtures.roleId(tenant, DefaultRoles.ADMIN))))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/users/{id}", admin.id()).with(hr.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(staffBody(admin.phone(), hrRole)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/users/{id}/disable", admin.id()).with(hr.token()))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/users").with(hr.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(staffBody(TestData.phone(), hrRole)))
                .andExpect(status().isCreated());
    }

    @Test
    void nobodyChangesTheirOwnRole() throws Exception {
        mvc.perform(put("/api/v1/users/{id}", admin.id()).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(staffBody(admin.phone(), fixtures.roleId(tenant, DefaultRoles.COUNSELLOR))))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/me").with(admin.token())).andExpect(jsonPath("$.role.code").value(DefaultRoles.ADMIN));
    }

    @Test
    void resettingSignInLetsThePersonLinkANewIdentity() throws Exception {
        mvc.perform(get("/api/v1/me").with(receptionist.token())).andExpect(status().isOk()); // linked to sub-<phone>

        mvc.perform(post("/api/v1/users/{id}/reset-sign-in", receptionist.id()).with(receptionist.token()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/users/{id}/reset-sign-in", receptionist.id()).with(admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVITED"));

        // e.g. their identity-provider account was recreated: new subject, same phone
        mvc.perform(get("/api/v1/me").with(TestData.otpToken("recreated", receptionist.phone())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(receptionist.id().toString()));
        mvc.perform(get("/api/v1/me").with(receptionist.token())).andExpect(status().isForbidden());
    }

    private static String staffBody(String phone, java.util.UUID roleId) {
        return """
                {"fullName":"Some One","phone":"%s","roleId":"%s"}""".formatted(phone, roleId);
    }
}
