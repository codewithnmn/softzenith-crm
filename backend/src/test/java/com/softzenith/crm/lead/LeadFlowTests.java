package com.softzenith.crm.lead;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.Fixtures.Staff;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.tenancy.Tenant;
import jakarta.mail.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Phase 1 end to end: enquiry → lead → notifications → assignment/status, with the permission rules. */
@IntegrationTest
class LeadFlowTests {

    @RegisterExtension
    static GreenMailExtension mail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    Tenant tenant;
    Staff admin;
    Staff receptionist;
    Staff counsellor;

    @BeforeEach
    void setUp() {
        tenant = fixtures.tenant();
        admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        receptionist = fixtures.staff(tenant, DefaultRoles.RECEPTIONIST);
        counsellor = fixtures.staff(tenant, DefaultRoles.COUNSELLOR);
    }

    @Test
    void publicEnquiryBecomesALeadAndNotifiesTheEnquirerAdminAndReception() throws Exception {
        var phone = TestData.phone();
        var email = "student-" + phone.substring(5) + "@mail.test";

        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Asha Verma", "phone", phone.substring(3), "email", email,
                                "serviceInterest", "Study Abroad", "preferredCountry", "Canada"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").doesNotExist());

        var leadId = leadIdByPhone(phone, receptionist);
        // Unassigned: a counsellor (OWN scope) does not see it yet.
        mvc.perform(get("/api/v1/leads/{id}", leadId).with(counsellor.token())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/leads/{id}", leadId).with(receptionist.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.sourceType").value("WEBSITE_FORM"))
                .andExpect(jsonPath("$.phone").value(phone));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(notifications(leadId)).hasSize(4));
        var sent = notifications(leadId);
        assertThat(sent).extracting(n -> n.get("channel") + "/" + n.get("kind") + "/" + n.get("recipient") + "/" + n.get("status"))
                .containsExactlyInAnyOrder(
                        "EMAIL/LEAD_WELCOME/" + email + "/SENT",
                        "WHATSAPP/LEAD_WELCOME/" + phone + "/DEMO",
                        "EMAIL/NEW_LEAD_ALERT/" + admin.email() + "/SENT",
                        "EMAIL/NEW_LEAD_ALERT/" + receptionist.email() + "/SENT");

        assertThat(subjectsTo(email)).anyMatch(s -> s.startsWith("Thank you for contacting Acme Visas"));
        // This tenant offers no service options, so the enquirer's free text is not repeated back to them.
        assertThat(sent).filteredOn(n -> "LEAD_WELCOME".equals(n.get("kind")))
                .allSatisfy(n -> assertThat((String) n.get("body")).doesNotContain("Study Abroad").doesNotContain("Canada"));
        assertThat(subjectsTo(admin.email())).anyMatch(s -> s.contains("Asha Verma"));
        assertThat(subjectsTo(counsellor.email())).isEmpty();
    }

    @Test
    void aRepeatEnquiryIsAddedToTheOpenLeadInsteadOfDuplicating() throws Exception {
        var phone = TestData.phone();
        var body = json(Map.of("fullName", "Ravi", "phone", phone, "email", "Ravi@Mail.test"));
        var first = submit(body);
        var second = submit(json(Map.of("fullName", "Ravi K", "phone", phone, "email", "ravi@mail.test", "message", "Any update?")));
        assertThat(second).isEqualTo(first); // same answer: the form does not reveal an existing enquiry

        var leadId = leadIdByPhone(phone, admin);
        mvc.perform(get("/api/v1/leads/{id}/activities", leadId).with(admin.token()))
                .andExpect(jsonPath("$[*].type", hasItem("REPEAT_ENQUIRY")));
        mvc.perform(get("/api/v1/leads").param("q", phone).with(admin.token()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void adminAndReceptionAssignReceptionCannotChangeStatus() throws Exception {
        var leadId = leadIdByPhone(submitAndGetPhone(), admin);
        var assignToCounsellor = json(Map.of("userId", counsellor.id().toString()));

        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(counsellor.token())
                        .contentType(MediaType.APPLICATION_JSON).content(assignToCounsellor))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(receptionist.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "CONTACTED"))))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("userId", receptionist.id().toString()))))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(receptionist.token())
                        .contentType(MediaType.APPLICATION_JSON).content(assignToCounsellor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTo.id").value(counsellor.id().toString()));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(notifications(leadId)).anyMatch(n -> "LEAD_ASSIGNED".equals(n.get("kind"))
                        && counsellor.email().equals(n.get("recipient"))));

        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "CLOSED"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "CLOSED", "reason", "Not interested"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedReason").value("Not interested"));
        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content(assignToCounsellor))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "NEW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.assignedTo").doesNotExist());

        // Reopened leads go back to the unassigned queue: the previous counsellor no longer sees it.
        mvc.perform(get("/api/v1/leads/{id}", leadId).with(counsellor.token())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/leads").param("unassigned", "true").param("q", leadIdPhone(leadId)).with(receptionist.token()))
                .andExpect(jsonPath("$.items[0].id").value(leadId));
        mvc.perform(get("/api/v1/leads/{id}/activities", leadId).with(admin.token()))
                .andExpect(jsonPath("$[?(@.type == 'ASSIGNED')].toValue", hasItem("Test COUNSELLOR")))
                .andExpect(jsonPath("$[?(@.type == 'UNASSIGNED')].fromValue", hasItem("Test COUNSELLOR")));
    }

    @Test
    void adminSeesLeadCountsByStatusSourceAndAssignee() throws Exception {
        var a = leadIdByPhone(submitAndGetPhone(), admin);
        var b = leadIdByPhone(submitAndGetPhone(), admin);
        submitAndGetPhone();
        mvc.perform(post("/api/v1/leads").with(receptionist.token()).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("fullName", "Walk In", "phone", TestData.phone(), "sourceType", "WALK_IN"))));
        mvc.perform(put("/api/v1/leads/{id}/assignment", a).with(receptionist.token())
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("userId", counsellor.id().toString()))));
        mvc.perform(post("/api/v1/leads/{id}/status", b).with(admin.token())
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "CLOSED", "reason", "Duplicate"))));

        mvc.perform(get("/api/v1/leads/stats").with(admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.byStatus.NEW").value(2))
                .andExpect(jsonPath("$.byStatus.ASSIGNED").value(1))
                .andExpect(jsonPath("$.byStatus.CLOSED").value(1))
                .andExpect(jsonPath("$.byStatus.CONTACTED").value(0))
                .andExpect(jsonPath("$.bySource.WEBSITE_FORM").value(3))
                .andExpect(jsonPath("$.bySource.WALK_IN").value(1))
                .andExpect(jsonPath("$.openUnassigned").value(2))
                .andExpect(jsonPath("$.byAssignee[0].name").value("Test COUNSELLOR"))
                .andExpect(jsonPath("$.byAssignee[0].count").value(1));

        mvc.perform(get("/api/v1/leads/stats").param("from", "2999-01-01T00:00:00Z").with(admin.token()))
                .andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/leads/stats").with(receptionist.token())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/leads/stats").with(counsellor.token())).andExpect(status().isForbidden());
    }

    @Test
    void staffRecordWalkInLeads() throws Exception {
        var phone = TestData.phone();
        mvc.perform(post("/api/v1/leads").with(receptionist.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Walk In", "phone", phone, "sourceType", "WALK_IN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceType").value("WALK_IN"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void leadsAreInvisibleToOtherTenants() throws Exception {
        var leadId = leadIdByPhone(submitAndGetPhone(), admin);
        var otherTenant = fixtures.tenant();
        var otherAdmin = fixtures.staff(otherTenant, DefaultRoles.ADMIN);

        mvc.perform(get("/api/v1/leads/{id}", leadId).with(otherAdmin.token())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/leads").with(otherAdmin.token())).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void enquiryFormDescribesTheTenantAndUnknownTenantsAre404() throws Exception {
        fixtures.branch(tenant, "Delhi");
        mvc.perform(get("/api/v1/public/tenants/{slug}/enquiry-form", tenant.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantName").value("Acme Visas"))
                .andExpect(jsonPath("$.branches[0].name").value("Delhi"));
        mvc.perform(get("/api/v1/public/tenants/{slug}/enquiry-form", "no-such-tenant"))
                .andExpect(status().isNotFound());
    }

    @Test
    void aRepeatEnquiryMovesTheLeadToTheTopAlertsStaffAndAcknowledgesTheEnquirer() throws Exception {
        var phone = TestData.phone();
        var email = "repeat-" + phone.substring(5) + "@mail.test";
        submit(json(Map.of("fullName", "Meera", "phone", phone, "email", email)));
        var leadId = leadIdByPhone(phone, admin);
        submitAndGetPhone(); // a newer lead, now on top

        var again = mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Meera", "phone", phone, "email", email, "message", "Still waiting"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("will get in touch")))
                .andReturn();
        assertThat(again.getResponse().getContentAsString()).doesNotContain("LD-").doesNotContain("existing");

        mvc.perform(get("/api/v1/leads").with(admin.token()))
                .andExpect(jsonPath("$.items[0].id").value(leadId))
                .andExpect(jsonPath("$.items[0].enquiryCount").value(2));

        // Unassigned: the new-lead alert recipients hear about it; the enquirer gets an acknowledgement.
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(kinds(leadId)).contains(
                "EMAIL/REPEAT_ENQUIRY_ALERT/" + admin.email(),
                "EMAIL/REPEAT_ENQUIRY_ALERT/" + receptionist.email(),
                "EMAIL/REPEAT_ENQUIRY_ACK/" + email,
                "WHATSAPP/REPEAT_ENQUIRY_ACK/" + phone));

        // Assigned: only the counsellor who owns the lead is alerted.
        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(receptionist.token())
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("userId", counsellor.id().toString()))))
                .andExpect(status().isOk());
        submit(json(Map.of("fullName", "Meera", "phone", phone, "email", email)));
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(kinds(leadId))
                .filteredOn(k -> k.startsWith("EMAIL/REPEAT_ENQUIRY_ALERT/")).hasSize(3)
                .contains("EMAIL/REPEAT_ENQUIRY_ALERT/" + counsellor.email()));
        assertThat(subjectsTo(email)).anyMatch(s -> s.startsWith("We have your message"));
    }

    @Test
    void counsellorChangesStatusAndTheEnquirerIsToldWithoutTheInternalReason() throws Exception {
        var phone = TestData.phone();
        var email = "status-" + phone.substring(5) + "@mail.test";
        submit(json(Map.of("fullName", "Kabir Das", "phone", phone, "email", email)));
        var leadId = leadIdByPhone(phone, admin);

        mvc.perform(put("/api/v1/leads/{id}/assignment", leadId).with(receptionist.token())
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("userId", counsellor.id().toString()))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(counsellor.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "CONTACTED"))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(counsellor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "CLOSED", "reason", "Budget too low - internal note"))))
                .andExpect(status().isOk());
        // Reopening still needs LEAD_REOPEN (Admin).
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(counsellor.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "NEW"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(admin.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "NEW"))))
                .andExpect(status().isOk());

        // assigned + contacted + closed + reopened, each by email and WhatsApp
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(notifications(leadId))
                .filteredOn(n -> "LEAD_STATUS_UPDATE".equals(n.get("kind"))).hasSize(8));
        var bodies = notifications(leadId).stream().filter(n -> "LEAD_STATUS_UPDATE".equals(n.get("kind")))
                .map(n -> (String) n.get("body")).toList();
        assertThat(bodies).anyMatch(b -> b.contains("Test COUNSELLOR") && b.contains("your counsellor"));
        assertThat(bodies).anyMatch(b -> b.contains("in progress"));
        assertThat(bodies).anyMatch(b -> b.contains("has been closed"));
        assertThat(bodies).anyMatch(b -> b.contains("reopened"));
        assertThat(bodies).noneMatch(b -> b.contains("Budget too low"));
    }

    @Test
    void editsAndReopensCannotCreateASecondOpenLeadForOnePerson() throws Exception {
        var phoneA = submitAndGetPhone();
        var phoneB = submitAndGetPhone();
        var leadA = leadIdByPhone(phoneA, admin);
        var leadB = leadIdByPhone(phoneB, admin);

        // Moving lead B onto lead A's person is refused, naming the lead to use instead.
        mvc.perform(put("/api/v1/leads/{id}", leadB).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Test Lead", "phone", phoneA))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("is already open")));
        // Saving a lead with its own phone is not a conflict with itself.
        mvc.perform(put("/api/v1/leads/{id}", leadA).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Test Lead Renamed", "phone", phoneA))))
                .andExpect(status().isOk());

        // Closed lead A, then the same person enquires again: a new open lead. Reopening A would make two.
        mvc.perform(post("/api/v1/leads/{id}/status", leadA).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "CLOSED", "reason", "No response"))))
                .andExpect(status().isOk());
        submit(json(Map.of("fullName", "Test Lead", "phone", phoneA)));
        mvc.perform(post("/api/v1/leads/{id}/status", leadA).with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "NEW"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("is already open")));
    }

    @Test
    void anonymousCallersCannotReadLeads() throws Exception {
        mvc.perform(get("/api/v1/leads")).andExpect(status().isUnauthorized());
    }

    private String submitAndGetPhone() throws Exception {
        var phone = TestData.phone();
        submit(json(Map.of("fullName", "Test Lead", "phone", phone)));
        return phone;
    }

    private String submit(String body) throws Exception {
        var result = mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.message");
    }

    private String leadIdByPhone(String phone, Staff as) throws Exception {
        var result = mvc.perform(get("/api/v1/leads").param("q", phone).with(as.token()))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.items[0].id");
    }

    private String leadIdPhone(String leadId) throws Exception {
        var result = mvc.perform(get("/api/v1/leads/{id}", leadId).with(admin.token()))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.phone");
    }

    private List<Map<String, Object>> notifications(String leadId) throws Exception {
        var result = mvc.perform(get("/api/v1/leads/{id}/notifications", leadId).with(admin.token()))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$");
    }

    /** channel/kind/recipient of every notification on the lead */
    private List<String> kinds(String leadId) throws Exception {
        return notifications(leadId).stream()
                .map(n -> n.get("channel") + "/" + n.get("kind") + "/" + n.get("recipient")).toList();
    }

    private List<String> subjectsTo(String address) {
        return Arrays.stream(mail.getReceivedMessages())
                .filter(m -> {
                    try {
                        return Arrays.stream(m.getRecipients(Message.RecipientType.TO)).anyMatch(a -> a.toString().equals(address));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(m -> {
                    try {
                        return m.getSubject();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .toList();
    }

    private static String json(Map<String, ?> map) {
        var sb = new StringBuilder("{");
        map.forEach((k, v) -> sb.append(sb.length() > 1 ? "," : "").append('"').append(k).append("\":\"")
                .append(v.toString().replace("\"", "\\\"")).append('"'));
        return sb.append('}').toString();
    }
}
