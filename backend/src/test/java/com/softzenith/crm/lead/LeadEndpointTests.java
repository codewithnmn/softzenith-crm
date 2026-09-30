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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.EnumSet;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Functional tests for /api/v1/leads, one nested class per endpoint: the success path, then each way the request can be
 * refused. The end-to-end Phase 1 journey (enquiry â†’ notifications â†’ assign â†’ status) is in {@link LeadFlowTests}; who
 * sees which lead is in {@link LeadScopeTests}.
 */
@IntegrationTest
class LeadEndpointTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    Tenant tenant;
    Staff admin;
    Staff counsellor;
    Staff noLeadAccess;

    @BeforeEach
    void setUp() {
        tenant = fixtures.tenant();
        admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        counsellor = fixtures.staff(tenant, DefaultRoles.COUNSELLOR);
        fixtures.role(tenant, "HR_ONLY", DataScope.ALL, false, EnumSet.of(Permission.USER_VIEW));
        noLeadAccess = fixtures.staff(tenant, "HR_ONLY");
    }

    @Nested
    class ListLeads {
        @Test
        void filtersByStatusSourceBranchAndAssignee() throws Exception {
            var branch = fixtures.branch(tenant, "Rohini");
            var walkIn = create("Walk In", TestData.phone(), "WALK_IN", branch.getId());
            var phoneCall = create("Phone Call", TestData.phone(), "PHONE", null);
            assign(phoneCall, counsellor);

            expectOnly(get("/api/v1/leads").param("sourceType", "WALK_IN"), walkIn);
            expectOnly(get("/api/v1/leads").param("branchId", branch.getId().toString()), walkIn);
            expectOnly(get("/api/v1/leads").param("assignedTo", counsellor.id().toString()), phoneCall);
            expectOnly(get("/api/v1/leads").param("status", "ASSIGNED"), phoneCall);
            expectOnly(get("/api/v1/leads").param("unassigned", "true"), walkIn);
        }

        @Test
        void searchesByNameEmailLeadNumberAndPhoneDigits() throws Exception {
            var phone = TestData.phone();
            var id = create("Zoya Qureshi", phone, "WALK_IN", null, "zoya.q@mail.test");
            var number = leadNumber(id);

            expectOnly(get("/api/v1/leads").param("q", "zoya q"), id);
            expectOnly(get("/api/v1/leads").param("q", "ZOYA.Q@MAIL"), id);
            expectOnly(get("/api/v1/leads").param("q", number.toLowerCase()), id);
            expectOnly(get("/api/v1/leads").param("q", phone.substring(6)), id);
            // Fewer than 4 digits is not treated as a phone search.
            mvc.perform(get("/api/v1/leads").param("q", phone.substring(10)).with(admin.token()))
                    .andExpect(jsonPath("$.items[*].id", not(hasItem(id))));
        }

        @Test
        void filtersByCreationTime() throws Exception {
            var id = create("Time Test", TestData.phone(), "WALK_IN", null);
            expectOnly(get("/api/v1/leads").param("createdFrom", "2000-01-01T00:00:00Z").param("createdTo", "2999-01-01T00:00:00Z"), id);
            mvc.perform(get("/api/v1/leads").param("createdFrom", "2999-01-01T00:00:00Z").with(admin.token()))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void pageSizeIsClampedBetween1And100() throws Exception {
            create("One", TestData.phone(), "WALK_IN", null);
            create("Two", TestData.phone(), "WALK_IN", null);
            mvc.perform(get("/api/v1/leads").param("size", "0").param("page", "-3").with(admin.token()))
                    .andExpect(jsonPath("$.size").value(1))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalPages").value(2));
            mvc.perform(get("/api/v1/leads").param("size", "5000").with(admin.token()))
                    .andExpect(jsonPath("$.size").value(100));
        }

        @Test
        void needsSignInAndLeadView() throws Exception {
            mvc.perform(get("/api/v1/leads")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/v1/leads").with(noLeadAccess.token())).andExpect(status().isForbidden());
        }
    }

    @Nested
    class CreateLead {
        @Test
        void keepsSourceDetailAndCustomFields() throws Exception {
            mvc.perform(post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content("""
                            {"fullName":"Ref Lead","phone":"%s","sourceType":"REFERRAL","sourceDetail":"Referred by Ravi",
                             "customFields":{"ieltsBand":"7.5","passport":true}}""".formatted(TestData.phone())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sourceType").value("REFERRAL"))
                    .andExpect(jsonPath("$.sourceDetail").value("Referred by Ravi"))
                    .andExpect(jsonPath("$.customFields.ieltsBand").value("7.5"))
                    .andExpect(jsonPath("$.customFields.passport").value(true));
        }

        @Test
        void aRepeatFromStaffReturnsTheExistingOpenLead() throws Exception {
            var phone = TestData.phone();
            var first = create("Same Person", phone, "WALK_IN", null);
            var second = create("Same Person", phone, "PHONE", null);
            org.assertj.core.api.Assertions.assertThat(second).isEqualTo(first);
        }

        @Test
        void refusesBadNamesUnknownOrClosedBranchesAndOversizedCustomFields() throws Exception {
            mvc.perform(createRequest("{\"fullName\":\"R2D2\",\"phone\":\"%s\",\"sourceType\":\"WALK_IN\"}".formatted(TestData.phone())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("fullName")));
            mvc.perform(createRequest("{\"fullName\":\"No Source\",\"phone\":\"%s\"}".formatted(TestData.phone())))
                    .andExpect(status().isBadRequest());
            mvc.perform(createRequest("""
                            {"fullName":"Lost","phone":"%s","sourceType":"WALK_IN","branchId":"%s"}""".formatted(TestData.phone(), UUID.randomUUID())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Unknown branch"));

            var closed = fixtures.branch(tenant, "Closed Office");
            mvc.perform(put("/api/v1/branches/{id}", closed.getId()).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Closed Office\",\"active\":false}")).andExpect(status().isOk());
            mvc.perform(createRequest("""
                            {"fullName":"Late","phone":"%s","sourceType":"WALK_IN","branchId":"%s"}""".formatted(TestData.phone(), closed.getId())))
                    .andExpect(status().isBadRequest());

            var fields = new StringBuilder();
            for (int i = 0; i < 51; i++) fields.append(i == 0 ? "" : ",").append("\"f").append(i).append("\":1");
            mvc.perform(createRequest("""
                            {"fullName":"Many Fields","phone":"%s","sourceType":"WALK_IN","customFields":{%s}}""".formatted(TestData.phone(), fields)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Custom fields")));
        }

        @Test
        void needsLeadCreate() throws Exception {
            mvc.perform(post("/api/v1/leads").with(noLeadAccess.token()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fullName\":\"X\",\"phone\":\"%s\",\"sourceType\":\"WALK_IN\"}".formatted(TestData.phone())))
                    .andExpect(status().isForbidden());
        }

        private org.springframework.test.web.servlet.RequestBuilder createRequest(String body) {
            return post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body);
        }
    }

    @Nested
    class UpdateLead {
        @Test
        void updatesContactDetailsAndRecordsAnActivity() throws Exception {
            var id = create("Old Name", TestData.phone(), "WALK_IN", null);
            var newPhone = TestData.phone();
            mvc.perform(put("/api/v1/leads/{id}", id).with(admin.token()).contentType(MediaType.APPLICATION_JSON).content("""
                            {"fullName":"New Name","phone":"%s","email":"New@Mail.test","serviceInterest":"Study Abroad",
                             "preferredCountry":"Canada","customFields":{"intake":"Jan 2027"}}""".formatted(newPhone)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("New Name"))
                    .andExpect(jsonPath("$.phone").value(newPhone))
                    .andExpect(jsonPath("$.email").value("new@mail.test"))
                    .andExpect(jsonPath("$.preferredCountry").value("Canada"))
                    .andExpect(jsonPath("$.customFields.intake").value("Jan 2027"));
            mvc.perform(get("/api/v1/leads/{id}/activities", id).with(admin.token()))
                    .andExpect(jsonPath("$[*].type", hasItem("UPDATED")));
        }

        @Test
        void aClosedLeadMayShareThePersonOfAnOpenOne() throws Exception {
            var openPhone = TestData.phone();
            create("Open One", openPhone, "WALK_IN", null);
            var closed = create("Closed One", TestData.phone(), "WALK_IN", null);
            changeStatus(closed, "{\"status\":\"CLOSED\",\"reason\":\"Duplicate\"}").andExpect(status().isOk());

            mvc.perform(put("/api/v1/leads/{id}", closed).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fullName\":\"Closed One\",\"phone\":\"%s\"}".formatted(openPhone)))
                    .andExpect(status().isOk());
        }

        @Test
        void unknownOrOutOfScopeLeadIs404AndNoEditPermissionIs403() throws Exception {
            var body = "{\"fullName\":\"X\",\"phone\":\"%s\"}".formatted(TestData.phone());
            mvc.perform(put("/api/v1/leads/{id}", UUID.randomUUID()).with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound());
            var unassigned = create("Not Mine", TestData.phone(), "WALK_IN", null);
            mvc.perform(put("/api/v1/leads/{id}", unassigned).with(counsellor.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound());
            mvc.perform(put("/api/v1/leads/{id}", unassigned).with(noLeadAccess.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class ChangeStatus {
        @Test
        void refusesAssignedTheSameStatusAndAMissingStatus() throws Exception {
            var id = create("Status Rules", TestData.phone(), "WALK_IN", null);
            changeStatus(id, "{\"status\":\"ASSIGNED\"}").andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail", containsString("Assign")));
            changeStatus(id, "{\"status\":\"NEW\"}").andExpect(status().isConflict());
            changeStatus(id, "{}").andExpect(status().isBadRequest());
        }

        @Test
        void unknownLeadIs404() throws Exception {
            mvc.perform(post("/api/v1/leads/{id}/status", UUID.randomUUID()).with(admin.token())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONTACTED\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Assign {
        @Test
        void unknownAssigneeIs404AndADisabledOneIs409() throws Exception {
            var id = create("Assign Rules", TestData.phone(), "WALK_IN", null);
            mvc.perform(put("/api/v1/leads/{id}/assignment", id).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"" + UUID.randomUUID() + "\"}"))
                    .andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/users/{id}/disable", counsellor.id()).with(admin.token())).andExpect(status().isOk());
            mvc.perform(put("/api/v1/leads/{id}/assignment", id).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"" + counsellor.id() + "\"}"))
                    .andExpect(status().isConflict());
        }

        @Test
        void reassigningRecordsBothCounsellors() throws Exception {
            var other = fixtures.staff(tenant, DefaultRoles.COUNSELLOR);
            var id = create("Reassign", TestData.phone(), "WALK_IN", null);
            assign(id, counsellor);
            assign(id, other);
            mvc.perform(get("/api/v1/leads/{id}/activities", id).with(admin.token()))
                    .andExpect(jsonPath("$[?(@.type == 'ASSIGNED')].fromValue", hasItem("Test COUNSELLOR")));
            mvc.perform(get("/api/v1/leads/{id}", id).with(counsellor.token())).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/leads/{id}", id).with(other.token())).andExpect(status().isOk());
        }
    }

    @Nested
    class Stats {
        @Test
        void aPeriodWithAnUpperBoundOnlyCountsEarlierLeads() throws Exception {
            create("Counted", TestData.phone(), "WALK_IN", null);
            mvc.perform(get("/api/v1/leads/stats").param("to", "2000-01-01T00:00:00Z").with(admin.token()))
                    .andExpect(jsonPath("$.total").value(0));
            mvc.perform(get("/api/v1/leads/stats").param("from", "2000-01-01T00:00:00Z").with(admin.token()))
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.byStatus.NEW").value(1));
        }

        @Test
        void needsSignIn() throws Exception {
            mvc.perform(get("/api/v1/leads/stats")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Activities {
        @Test
        void listsHistoryOldestFirstWithTheActorsName() throws Exception {
            var id = create("History", TestData.phone(), "WALK_IN", null);
            assign(id, counsellor);
            mvc.perform(get("/api/v1/leads/{id}/activities", id).with(admin.token()))
                    .andExpect(jsonPath("$[0].type").value("CREATED"))
                    .andExpect(jsonPath("$[0].toValue").value("WALK_IN"))
                    .andExpect(jsonPath("$[1].type").value("ASSIGNED"))
                    .andExpect(jsonPath("$[1].actor.name").value("Test ADMIN"))
                    .andExpect(jsonPath("$[*].type", everyItem(is(org.hamcrest.Matchers.oneOf("CREATED", "ASSIGNED")))));
        }

        @Test
        void unknownLeadIs404() throws Exception {
            mvc.perform(get("/api/v1/leads/{id}/activities", UUID.randomUUID()).with(admin.token()))
                    .andExpect(status().isNotFound());
        }
    }

    private void expectOnly(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String id)
            throws Exception {
        mvc.perform(request.with(admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(id));
    }

    private String create(String name, String phone, String source, UUID branchId) throws Exception {
        return create(name, phone, source, branchId, null);
    }

    private String create(String name, String phone, String source, UUID branchId, String email) throws Exception {
        var body = "{\"fullName\":\"%s\",\"phone\":\"%s\",\"sourceType\":\"%s\"%s%s}".formatted(name, phone, source,
                branchId == null ? "" : ",\"branchId\":\"" + branchId + "\"",
                email == null ? "" : ",\"email\":\"" + email + "\"");
        var result = mvc.perform(post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String leadNumber(String id) throws Exception {
        var result = mvc.perform(get("/api/v1/leads/{id}", id).with(admin.token())).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.leadNumber");
    }

    private void assign(String id, Staff to) throws Exception {
        mvc.perform(put("/api/v1/leads/{id}/assignment", id).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + to.id() + "\"}"))
                .andExpect(status().isOk());
    }

    private ResultActions changeStatus(String id, String body) throws Exception {
        return mvc.perform(post("/api/v1/leads/{id}/status", id).with(admin.token())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
